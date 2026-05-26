package com.boxoffice.deliverymanagerservice.controller;

import com.boxoffice.common.response.ApiResponse;
import com.boxoffice.deliverymanagerservice.dto.DeliveryManagerCreateRequestDto;
import com.boxoffice.deliverymanagerservice.dto.DeliveryManagerResponseDto;
import com.boxoffice.deliverymanagerservice.dto.DeliveryManagerUpdateRequestDto;
import com.boxoffice.deliverymanagerservice.service.DeliveryManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/delivery-managers")
@RequiredArgsConstructor
public class DeliveryManagerController {

    private final DeliveryManagerService deliveryManagerService;

    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryManagerResponseDto>> createDeliveryManager(
            @RequestBody DeliveryManagerCreateRequestDto request) {
        log.info("[Controller] 배송 담당자 등록 요청 수신. UserId: {}", request.getUserId());
        DeliveryManagerResponseDto response = deliveryManagerService.createDeliveryManager(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryManagerResponseDto>> getDeliveryManager(
            @PathVariable UUID id) {
        log.info("[Controller] 배송 담당자 단건 조회 요청 수신. ManagerId: {}", id);
        DeliveryManagerResponseDto response = deliveryManagerService.getDeliveryManager(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryManagerResponseDto>> updateDeliveryManager(
            @PathVariable UUID id,
            @RequestBody DeliveryManagerUpdateRequestDto request) {
        log.info("[Controller] 배송 담당자 정보 수정 요청 수신. ManagerId: {}", id);
        DeliveryManagerResponseDto response = deliveryManagerService.updateDeliveryManager(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    public ResponseEntity<ApiResponse<Void>> deleteDeliveryManager(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String requesterId) {
        log.info("[Controller] 배송 담당자 삭제 요청 수신. ManagerId: {}, RequesterId: {}", id, requesterId);
        deliveryManagerService.deleteDeliveryManager(id, requesterId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}