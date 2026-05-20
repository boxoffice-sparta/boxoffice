package com.boxoffice.companyservice.company.service;

import com.boxoffice.common.exception.BaseException;
import com.boxoffice.common.exception.CommonErrorCode;
import com.boxoffice.companyservice.company.dto.request.CompanyCreateRequestDto;
import com.boxoffice.companyservice.company.dto.response.CompanyCreateResponseDto;
import com.boxoffice.companyservice.company.exception.CompanyErrorCode;
import com.boxoffice.companyservice.company.validator.HubValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyFacade 테스트")
class CompanyFacadeTest {

    @InjectMocks
    private CompanyFacade companyFacade;

    @Mock
    private HubValidator hubValidator;

    @Mock
    private CompanyService companyService;

    @ParameterizedTest(name = "성공 - role={0}이면 Hub 검증 후 업체를 생성한다")
    @ValueSource(strings = {"MASTER", "HUB_MANAGER", " hub_manager "})
    void createCompanyWithAllowedRole(String userRole) {
        // given
        // " hub_manager "는 공백/소문자가 섞인 role도 정규화되어 허용되는지 확인하는 케이스다.
        CompanyCreateRequestDto request = createRequest(UUID.randomUUID());
        CompanyCreateResponseDto expectedResponse = mock(CompanyCreateResponseDto.class);
        when(companyService.createCompany(request)).thenReturn(expectedResponse);

        // when
        CompanyCreateResponseDto response = companyFacade.createCompany(request, userRole);

        // then
        assertThat(response).isSameAs(expectedResponse);
        // Facade는 권한 통과 후 Hub 검증을 먼저 하고, 그 다음 저장 서비스로 위임해야 한다.
        InOrder inOrder = inOrder(hubValidator, companyService);
        inOrder.verify(hubValidator).validateHubActive(request.getHubId());
        inOrder.verify(companyService).createCompany(request);
        // 성공 흐름에서 의도한 두 호출 외에 다른 협력 객체 호출이 없어야 한다.
        verifyNoMoreInteractions(hubValidator, companyService);
    }

    @Test
    @DisplayName("실패 - Hub 검증이 실패하면 예외를 전파하고 업체를 저장하지 않는다")
    void createCompanyWhenHubValidationFails() {
        // given
        CompanyCreateRequestDto request = createRequest(UUID.randomUUID());
        BaseException hubException = new BaseException(CompanyErrorCode.HUB_INACTIVE);
        // 권한 통과 후 Hub 검증에서 실패하면 DB 저장 단계로 넘어가면 안 된다.
        doThrow(hubException)
                .when(hubValidator)
                .validateHubActive(request.getHubId());

        // when
        Throwable throwable = catchThrowable(() -> companyFacade.createCompany(request, "MASTER"));

        // then
        assertThat(throwable).isSameAs(hubException);
        verify(hubValidator).validateHubActive(request.getHubId());
        verifyNoMoreInteractions(hubValidator);
        verifyNoInteractions(companyService);
    }

    @Test
    @DisplayName("실패 - role 헤더가 없으면 인증 실패로 처리하고 Hub 검증과 저장을 호출하지 않는다")
    void createCompanyWithoutRole() {
        // given
        CompanyCreateRequestDto request = createRequest(UUID.randomUUID());

        // when
        Throwable throwable = catchThrowable(() -> companyFacade.createCompany(request, null));

        // then
        assertThat(throwable)
                .isInstanceOfSatisfying(BaseException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.UNAUTHORIZED));
        verifyNoInteractions(hubValidator, companyService);
    }

    @Test
    @DisplayName("실패 - role 헤더가 공백이면 인증 실패로 처리하고 Hub 검증과 저장을 호출하지 않는다")
    void createCompanyWithBlankRole() {
        // given
        CompanyCreateRequestDto request = createRequest(UUID.randomUUID());

        // when
        Throwable throwable = catchThrowable(() -> companyFacade.createCompany(request, "   "));

        // then
        assertThat(throwable)
                .isInstanceOfSatisfying(BaseException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.UNAUTHORIZED));
        verifyNoInteractions(hubValidator, companyService);
    }

    @Test
    @DisplayName("실패 - DELIVERY_MANAGER는 업체를 생성할 수 없다")
    void createCompanyWithForbiddenRole() {
        // given
        CompanyCreateRequestDto request = createRequest(UUID.randomUUID());

        // when
        Throwable throwable = catchThrowable(() -> companyFacade.createCompany(request, "DELIVERY_MANAGER"));

        // then
        assertThat(throwable)
                .isInstanceOfSatisfying(BaseException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.FORBIDDEN));
        verifyNoInteractions(hubValidator, companyService);
    }

    private CompanyCreateRequestDto createRequest(UUID hubId) {
        CompanyCreateRequestDto request = new CompanyCreateRequestDto();
        // Request DTO는 setter가 없으므로 Facade 단위 테스트에서 필요한 hubId만 주입한다.
        ReflectionTestUtils.setField(request, "hubId", hubId);
        return request;
    }
}
