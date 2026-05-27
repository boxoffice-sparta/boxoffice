package com.boxoffice.deliverymanagerservice.controller;

import com.boxoffice.common.response.ApiResponse;
import com.boxoffice.deliverymanagerservice.dto.DeliveryAssignRequestDto;
import com.boxoffice.deliverymanagerservice.dto.DeliveryAssignResponseDto;
import com.boxoffice.deliverymanagerservice.service.DeliveryManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/internal/v1/delivery-managers")
@RequiredArgsConstructor
public class DeliveryManagerInternalController {

    private final DeliveryManagerService deliveryManagerService;

    @PatchMapping("/clear-hub/{hubId}")
    public ResponseEntity<ApiResponse<Void>> clearDeliveryManagerHubId(
            @PathVariable("hubId") UUID hubId) {

        log.info("[Internal Controller] 허브 삭제에 따른 기사님 hubId 초기화 요청 수신. TargetHubId: {}", hubId);
        deliveryManagerService.clearDeliveryManagerHubId(hubId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<DeliveryAssignResponseDto>> assignNextManager(
            @RequestBody DeliveryAssignRequestDto request) {

        log.info("[Internal Controller] 배달 담당자 자동 배정 요청 수신. HubId: {}", request.getHubId());
        DeliveryAssignResponseDto response = deliveryManagerService.assignNextDeliveryManager(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}