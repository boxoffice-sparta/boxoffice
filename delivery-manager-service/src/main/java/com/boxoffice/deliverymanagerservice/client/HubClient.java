package com.boxoffice.deliverymanagerservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "hub-service", path = "/internal/v1/hubs")
public interface HubClient {

    @GetMapping("/{hubId}/active")
    boolean checkHubActive(@PathVariable("hubId") UUID hubId);
}