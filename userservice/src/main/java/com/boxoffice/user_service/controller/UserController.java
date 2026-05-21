package com.boxoffice.user_service.controller;

import com.boxoffice.common.response.ApiResponse;
import com.boxoffice.user_service.dto.UserResponseDto;
import com.boxoffice.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
//: 내 정보 조회, 사용자 목록 검색, 회원 탈퇴 등 관리
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getUserList() {
        // 아직 DB 연동 전이므로, 토큰이 통과했을 때 보여줄 임시 데이터(Mock Data)를 반환합니다.
        List<Map<String, String>> mockUsers = List.of(
                Map.of("id", "1", "username", "user1", "name", "홍길동", "status", "인증 파이프라인 개통 완료! 🎉")
        );

        return ResponseEntity.ok(mockUsers);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getMyInfo(
            // 🌟 Gateway 필터가 조작해서 꽂아준 헤더 값을 그대로 받아옵니다!
            @RequestHeader("X-User-Id") String userId) {

        log.info("[Controller] 내 정보 조회 요청. 전달받은 UserId: {}", userId);

        UserResponseDto responseDto = userService.getMyInfo(userId);

        // 팀원의 공통 규격인 ApiResponse.success()를 사용하여 이쁘게 래핑
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }
}