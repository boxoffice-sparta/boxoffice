package com.boxoffice.deliverymanagerservice.service;

import com.boxoffice.common.exception.BaseException;
import com.boxoffice.deliverymanagerservice.dto.*;
import com.boxoffice.deliverymanagerservice.entity.DeliveryManager;
import com.boxoffice.deliverymanagerservice.exception.DeliveryManagerErrorCode;
import com.boxoffice.deliverymanagerservice.repository.DeliveryManagerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryManagerService {

    private final DeliveryManagerRepository deliveryManagerRepository;

    @Transactional
    public DeliveryManagerResponseDto createDeliveryManager(DeliveryManagerCreateRequestDto request) {

        if (deliveryManagerRepository.findByUserId(request.getUserId()).isPresent()) {
            log.warn("[DeliveryManagerCreate] 중복 등록 시도. UserId: {}", request.getUserId());
            throw new BaseException(DeliveryManagerErrorCode.ALREADY_REGISTERED_MANAGER);
        }

        DeliveryManager newManager = DeliveryManager.builder()
                .userId(request.getUserId())
                .hubId(request.getHubId())
                .type(request.getType())
                .build();

        deliveryManagerRepository.save(newManager);
        log.info("[DeliveryManagerCreate] 배송 담당자 생성 완료. ManagerId: {}, UserId: {}", newManager.getId(), newManager.getUserId());

        return DeliveryManagerResponseDto.from(newManager);
    }


    @Transactional(readOnly = true)
    public DeliveryManagerResponseDto getDeliveryManager(UUID managerId) {
        DeliveryManager manager = deliveryManagerRepository.findById(managerId)
                .orElseThrow(() -> new BaseException(DeliveryManagerErrorCode.DELIVERY_MANAGER_NOT_FOUND));

        return DeliveryManagerResponseDto.from(manager);
    }

    @Transactional
    public DeliveryManagerResponseDto updateDeliveryManager(UUID managerId, DeliveryManagerUpdateRequestDto request) {
        DeliveryManager manager = deliveryManagerRepository.findById(managerId)
                .orElseThrow(() -> new BaseException(DeliveryManagerErrorCode.DELIVERY_MANAGER_NOT_FOUND));

        if (request.getHubId() != null) {
            manager.updateHub(request.getHubId());
        }
        if (request.getType() != null) {
            manager.updateType(request.getType());
        }

        log.info("[DeliveryManagerUpdate] 배송 담당자 정보 수정 완료. ManagerId: {}", managerId);
        return DeliveryManagerResponseDto.from(manager);
    }

    @Transactional
    public void deleteDeliveryManager(UUID managerId, String requesterId) {
        DeliveryManager manager = deliveryManagerRepository.findById(managerId)
                .orElseThrow(() -> new BaseException(DeliveryManagerErrorCode.DELIVERY_MANAGER_NOT_FOUND));

        manager.softDelete(UUID.fromString(requesterId));

        log.info("[DeliveryManagerDelete] 배송 담당자 논리적 삭제 완료. ManagerId: {}, RequesterId: {}", managerId, requesterId);
    }
    /**
     * [Internal] 타 서비스 호출용: 라운드 로빈 기반 배송 담당자 자동 배정
     */
    @Transactional
    public DeliveryAssignResponseDto assignNextDeliveryManager(DeliveryAssignRequestDto request) {
        // 1. 조건에 맞는 기사님 중 가장 오래 쉰(lastAssignedAt이 가장 예전인) 기사님 1명 호출
        DeliveryManager manager = deliveryManagerRepository
                .findFirstByHubIdAndTypeAndIsDeletedFalseOrderByLastAssignedAtAsc(request.getHubId(), request.getType())
                .orElseThrow(() -> new BaseException(DeliveryManagerErrorCode.DELIVERY_MANAGER_NOT_FOUND));

        // 2. 마지막 배정 시간 최신화 (순번 맨 뒤로 밀림)
        manager.recordAssignment();
        log.info("[DeliveryManagerAssign] 기사님 자동 배정 완료. ManagerId: {}, HubId: {}", manager.getId(), request.getHubId());

        // 3. 배정된 기사님 ID 반환
        return new DeliveryAssignResponseDto(manager.getId());
    }
}