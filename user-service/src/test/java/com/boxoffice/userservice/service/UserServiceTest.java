package com.boxoffice.userservice.service;

import com.boxoffice.common.exception.BaseException;
import com.boxoffice.common.exception.CommonErrorCode;
import com.boxoffice.userservice.client.HubServiceClient;
import com.boxoffice.userservice.client.KeycloakClient;
import com.boxoffice.userservice.dto.*;
import com.boxoffice.userservice.entity.Email;
import com.boxoffice.userservice.entity.User;
import com.boxoffice.userservice.entity.UserRole;
import com.boxoffice.userservice.entity.UserStatus;
import com.boxoffice.userservice.exception.UserErrorCode;
import com.boxoffice.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeycloakClient keycloakClient;

    @Mock
    private HubServiceClient hubServiceClient;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    // 공통 더미 데이터
    private User masterUser;
    private User hubManagerUser;
    private final UUID masterId = UUID.randomUUID();
    private final UUID hubManagerId = UUID.randomUUID();
    private final UUID testHubId = UUID.randomUUID();
    private final String masterSub = "master-sub-1234";
    private final String hubManagerSub = "hub-manager-sub-5678";

    @BeforeEach
    void setUp() {
        // @Value 환경변수 강제 주입
        ReflectionTestUtils.setField(userService, "realm", "boxoffice-realm");
        ReflectionTestUtils.setField(userService, "adminUsername", "admin");
        ReflectionTestUtils.setField(userService, "adminPassword", "boxoffice123");
        ReflectionTestUtils.setField(userService, "adminClientId", "admin-cli");
        ReflectionTestUtils.setField(userService, "userClientId", "boxoffice-app");

        // 더미 MASTER 유저 셋팅
        masterUser = User.builder()
                .keycloakSub(masterSub)
                .email(new Email("master@test.com"))
                .name("마스터")
                .role(UserRole.MASTER)
                .status(UserStatus.APPROVED)
                .build();
        ReflectionTestUtils.setField(masterUser, "id", masterId);

        // 더미 HUB_MANAGER 유저 셋팅
        hubManagerUser = User.builder()
                .keycloakSub(hubManagerSub)
                .email(new Email("hubmanager@test.com"))
                .name("허브매니저")
                .role(UserRole.HUB_MANAGER)
                .status(UserStatus.APPROVED)
                .hubId(testHubId)
                .build();
        ReflectionTestUtils.setField(hubManagerUser, "id", hubManagerId);
    }

    // ================= [ 회원가입 (SignUp) 테스트 ] ================= //

    @Test
    @DisplayName("회원가입 성공 - 허브 소속 유저 (Hub 유효성 통과)")
    void signUp_Success_WithHub() {
        // given
        UserSignupRequestDto request = new UserSignupRequestDto(
                "testuser", "password", "test@test.com", "홍길동", UserRole.DELIVERY_MANAGER, testHubId.toString()
        );

        setupKeycloakMocks(true); // Keycloak 성공 응답 모킹
        when(hubServiceClient.checkHubActive(testHubId)).thenReturn(true); // 허브 살아있음
        when(userRepository.save(any(User.class))).thenReturn(masterUser);

        // when
        assertDoesNotThrow(() -> userService.signUp(request));

        // then
        verify(hubServiceClient, times(1)).checkHubActive(testHubId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 존재하지 않거나 비활성화된 허브 (보상 트랜잭션 발동)")
    void signUp_Fail_InactiveHub() {
        // given
        UserSignupRequestDto request = new UserSignupRequestDto(
                "testuser", "password", "test@test.com", "홍길동", UserRole.DELIVERY_MANAGER, testHubId.toString()
        );

        setupKeycloakMocks(true);
        when(hubServiceClient.checkHubActive(testHubId)).thenReturn(false); // 허브가 비활성 상태!

        // when & then
        BaseException ex = assertThrows(BaseException.class, () -> userService.signUp(request));
        assertEquals(CommonErrorCode.INVALID_INPUT, ex.getErrorCode());

        // 🌟 핵심 검증: 에러 발생 시 생성했던 Keycloak 유저를 다시 삭제(Rollback) 했는가?
        verify(keycloakClient, times(1)).deleteUser(anyString(), anyString(), anyString());
        verify(userRepository, never()).save(any(User.class)); // DB 저장은 시도조차 안 해야 함
    }

    @Test
    @DisplayName("회원가입 실패 - DB 저장 실패 시 보상 트랜잭션 발동")
    void signUp_Fail_DBSaveError() {
        // given
        UserSignupRequestDto request = new UserSignupRequestDto(
                "testuser", "password", "test@test.com", "홍길동", UserRole.MASTER, null
        );

        setupKeycloakMocks(true);
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("DB 터짐!"));

        // when & then
        BaseException ex = assertThrows(BaseException.class, () -> userService.signUp(request));
        assertEquals(CommonErrorCode.INTERNAL_SERVER_ERROR, ex.getErrorCode());

        // 🌟 핵심 검증: DB 에러 시 Keycloak 롤백
        verify(keycloakClient, times(1)).deleteUser(anyString(), anyString(), anyString());
    }

    // ================= [ 다건 조회 (Data Isolation) 테스트 ] ================= //

    @Test
    @DisplayName("유저 목록 조회 성공 - HUB_MANAGER는 자신의 허브 소속만 조회 (데이터 격리)")
    void getUserList_Success_HubManager() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);
        Page<User> mockPage = new PageImpl<>(List.of(hubManagerUser));

        when(userRepository.findByKeycloakSub(hubManagerSub)).thenReturn(Optional.of(hubManagerUser));
        when(userRepository.findByHubId(testHubId, pageable)).thenReturn(mockPage);

        // when
        Page<UserResponseDto> result = userService.getUserList(hubManagerSub, pageable);

        // then
        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).findByHubId(testHubId, pageable); // 허브 조건 쿼리가 호출되었는지 확인
        verify(userRepository, never()).findAll(pageable); // 전체 조회는 절대 호출되면 안 됨
    }

    @Test
    @DisplayName("유저 목록 조회 실패 - 권한 없음 (DELIVERY_MANAGER 등)")
    void getUserList_Fail_Forbidden() {
        // given
        User deliveryUser = User.builder().keycloakSub("delivery").email(new Email("d@d.com")).name("배달").role(UserRole.DELIVERY_MANAGER).build();
        when(userRepository.findByKeycloakSub("delivery")).thenReturn(Optional.of(deliveryUser));

        // when & then
        BaseException ex = assertThrows(BaseException.class, () -> userService.getUserList("delivery", PageRequest.of(0, 10)));
        assertEquals(CommonErrorCode.FORBIDDEN, ex.getErrorCode());
    }

    // ================= [ 유저 상태 변경 (Update Status) 테스트 ] ================= //

    @Test
    @DisplayName("유저 상태 변경 성공 - 허브 매니저 승인 시 Feign 통신 검증")
    void updateUserStatus_Success_ApproveHubManager() {
        // given
        User targetUser = User.builder()
                .keycloakSub("target")
                .email(new Email("target@test.com"))
                .name("대기중매니저")
                .role(UserRole.HUB_MANAGER)
                .status(UserStatus.PENDING)
                .hubId(testHubId)
                .build();
        ReflectionTestUtils.setField(targetUser, "id", UUID.randomUUID());

        UserStatusUpdateRequestDto request = new UserStatusUpdateRequestDto();
        ReflectionTestUtils.setField(request, "status", "APPROVED");

        when(userRepository.findByKeycloakSub(masterSub)).thenReturn(Optional.of(masterUser));
        when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));

        // when
        UserResponseDto result = userService.updateUserStatus(targetUser.getId(), masterSub, request);

        // then
        assertEquals("APPROVED", result.getStatus());
        // 상태가 APPROVED + 권한이 HUB_MANAGER 이므로 허브 서비스로 매니저 등록 요청을 보냈는지 검증!
        verify(hubServiceClient, times(1)).registerHubManager(eq(testHubId), any());
    }

    @Test
    @DisplayName("유저 상태 변경 실패 - 다른 허브의 매니저가 권한 남용 시도")
    void updateUserStatus_Fail_DifferentHub() {
        // given
        User targetUser = User.builder()
                .keycloakSub("target")
                .email(new Email("t@t.com"))
                .name("타허브유저")
                .role(UserRole.DELIVERY_MANAGER)
                .hubId(UUID.randomUUID()) // 허브매니저(testHubId)와 소속 허브가 다름!
                .build();
        ReflectionTestUtils.setField(targetUser, "id", UUID.randomUUID());

        when(userRepository.findByKeycloakSub(hubManagerSub)).thenReturn(Optional.of(hubManagerUser));
        when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));

        // when & then
        BaseException ex = assertThrows(BaseException.class, () -> userService.updateUserStatus(targetUser.getId(), hubManagerSub, new UserStatusUpdateRequestDto()));
        assertEquals(CommonErrorCode.FORBIDDEN, ex.getErrorCode());
    }

    // ================= [ 로그아웃 및 기타 테스트 ] ================= //

    @Test
    @DisplayName("로그아웃 성공 - JWT 페이로드 파싱 및 Redis 등록 검증")
    void logout_Success() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 가짜 JWT 토큰 생성 (미래의 만료 시간을 가진 페이로드)
        long futureExp = (System.currentTimeMillis() / 1000) + 3600;
        String payload = "{\"exp\": " + futureExp + "}";
        String encodedPayload = Base64.getUrlEncoder().encodeToString(payload.getBytes());
        String fakeJwt = "header." + encodedPayload + ".signature";

        // when
        assertDoesNotThrow(() -> userService.logout("Bearer " + fakeJwt));

        // then
        // Redis에 토큰이 블랙리스트로 등록되었는지 검증
        verify(valueOperations, times(1)).set(eq(fakeJwt), eq("logout"), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("업체 매핑 성공 - SUPPLIER_MANAGER 일 때만 허용")
    void updateUserCompany_Success() {
        // given
        User supplier = User.builder().keycloakSub("sup").email(new Email("s@s.com")).name("업체").role(UserRole.SUPPLIER_MANAGER).build();
        ReflectionTestUtils.setField(supplier, "id", UUID.randomUUID());

        UserCompanyUpdateRequestDto request = new UserCompanyUpdateRequestDto();
        ReflectionTestUtils.setField(request, "companyId", UUID.randomUUID());

        when(userRepository.findById(supplier.getId())).thenReturn(Optional.of(supplier));

        // when
        UserResponseDto result = userService.updateUserCompany(supplier.getId(), request);

        // then
        assertNotNull(result.getCompanyId());
    }

    // ====== Keycloak Mocks Helper ======
    private void setupKeycloakMocks(boolean isSuccess) {
        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("access_token", "dummy-admin-token");
        when(keycloakClient.getAdminToken(anyString(), anyMap())).thenReturn(tokenResponse);

        if (isSuccess) {
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create("http://localhost:8089/admin/realms/boxoffice-realm/users/new-sub-123"));
            when(keycloakClient.createUser(anyString(), anyString(), any())).thenReturn(new ResponseEntity<>(headers, HttpStatus.CREATED));
        }
    }
    // ================= [ 유저 조회 및 허브 초기화 추가 테스트 ] ================= //

    @Test
    @DisplayName("유저 ID로 단건 조회 성공")
    void getUserById_Success() {
        // given
        when(userRepository.findById(masterId)).thenReturn(Optional.of(masterUser));

        // when
        UserResponseDto result = userService.getUserById(masterId);

        // then
        assertEquals("마스터", result.getName());
        verify(userRepository, times(1)).findById(masterId);
    }

    @Test
    @DisplayName("Keycloak Sub로 단건 조회 성공")
    void getUserBySub_Success() {
        // given
        when(userRepository.findByKeycloakSub(masterSub)).thenReturn(Optional.of(masterUser));

        // when
        UserResponseDto result = userService.getUserBySub(masterSub);

        // then
        assertEquals(masterId, result.getId());
        verify(userRepository, times(1)).findByKeycloakSub(masterSub);
    }

    @Test
    @DisplayName("허브 삭제 시 유저 허브 ID 일괄 초기화 성공")
    void clearUserHubId_Success() {
        // given
        doNothing().when(userRepository).clearHubIdByHubId(testHubId);

        // when
        assertDoesNotThrow(() -> userService.clearUserHubId(testHubId));

        // then
        verify(userRepository, times(1)).clearHubIdByHubId(testHubId);
    }
}