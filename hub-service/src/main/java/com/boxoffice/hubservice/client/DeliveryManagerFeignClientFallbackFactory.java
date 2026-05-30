package com.boxoffice.hubservice.client;

import com.boxoffice.common.exception.BaseException;
import com.boxoffice.common.exception.CommonErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class DeliveryManagerFeignClientFallbackFactory implements FallbackFactory<DeliveryManagerFeignClient> {

    @Override
    public DeliveryManagerFeignClient create(Throwable cause) {
        log.error("delivery-manager-service 호출 실패: {}", cause.getMessage());
        return new DeliveryManagerFeignClient() {

            @Override
            public void clearHub(UUID hubId) {
                throw new BaseException(CommonErrorCode.FEIGN_CLIENT_ERROR);
            }
        };
    }
}
