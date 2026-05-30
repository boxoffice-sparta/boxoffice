package com.boxoffice.hubservice.client;

import com.boxoffice.hubservice.client.fallback.DeliveryManagerFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "delivery-manager-service", fallbackFactory = DeliveryManagerFeignClientFallbackFactory.class)
public interface DeliveryManagerFeignClient {

    @PatchMapping("/internal/v1/delivery-managers/clear-hub/{hubId}")
    void clearHub(@PathVariable UUID hubId);
}
