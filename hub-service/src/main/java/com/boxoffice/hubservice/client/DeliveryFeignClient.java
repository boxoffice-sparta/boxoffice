package com.boxoffice.hubservice.client;

import com.boxoffice.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "delivery-service")
public interface DeliveryFeignClient {

    @GetMapping("/internal/v1/deliveries/active-count")
    ApiResponse<Integer> countActiveDeliveries(@RequestParam UUID hubId);
}
