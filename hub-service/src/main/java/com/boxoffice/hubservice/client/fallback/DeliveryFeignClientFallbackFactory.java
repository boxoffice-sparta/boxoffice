package com.boxoffice.hubservice.client.fallback;

import com.boxoffice.common.exception.BaseException;
import com.boxoffice.common.exception.CommonErrorCode;
import com.boxoffice.common.response.ApiResponse;
import com.boxoffice.hubservice.client.DeliveryFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class DeliveryFeignClientFallbackFactory implements FallbackFactory<DeliveryFeignClient> {

    @Override
    public DeliveryFeignClient create(Throwable cause) {
        log.error("delivery-service 호출 실패: {}", cause.getMessage());
        return new DeliveryFeignClient() {
            @Override
            public ApiResponse<Integer> countActiveDeliveries(UUID hubId) {
                throw new BaseException(CommonErrorCode.FEIGN_CLIENT_ERROR);
            }
        };
    }
}
