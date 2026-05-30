package com.boxoffice.deliverymanagerservice.service;

import com.boxoffice.common.exception.BaseException;
import com.boxoffice.deliverymanagerservice.client.HubClient;
import com.boxoffice.deliverymanagerservice.dto.*;
import com.boxoffice.deliverymanagerservice.entity.DeliveryManager;
import com.boxoffice.deliverymanagerservice.entity.ManagerStatus;
import com.boxoffice.deliverymanagerservice.exception.DeliveryManagerErrorCode;
import com.boxoffice.deliverymanagerservice.kafka.DeliveryNotificationProducer;
import com.boxoffice.deliverymanagerservice.kafka.event.DeliveryAssignedEvent;
import com.boxoffice.deliverymanagerservice.repository.DeliveryManagerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryManagerService {

    private final DeliveryManagerRepository deliveryManagerRepository;
    private final HubClient hubClient;
    private final DeliveryNotificationProducer notificationProducer;

    @Transactional
    public DeliveryManagerResponseDto createDeliveryManager(DeliveryManagerCreateRequestDto request, String role) {
        checkAdminRole(role);

        boolean isHubActive = hubClient.checkHubActive(request.getHubId());
        if (!isHubActive) {
            throw new BaseException(DeliveryManagerErrorCode.HUB_IS_NOT_ACTIVE);
        }

        if (deliveryManagerRepository.findByUserId(request.getUserId()).isPresent()) {
            log.warn("[DeliveryManagerCreate] 중복 등록 시도. UserId: {}", request.getUserId());
            throw new BaseException(DeliveryManagerErrorCode.ALREADY_REGISTERED_MANAGER);
        }

        DeliveryManager newManager = DeliveryManager.builder()
                .userId(request.getUserId())
                .hubId(request.getHubId())
                .type(request.getType())
                .slackId(null)
                .status(ManagerStatus.WAITING)
                .build();

        deliveryManagerRepository.save(newManager);
        return DeliveryManagerResponseDto.from(newManager);
    }


    @Transactional(readOnly = true)
    public DeliveryManagerResponseDto getDeliveryManager(UUID managerId, String requesterId, String role) {
        DeliveryManager manager = deliveryManagerRepository.findById(managerId)
                .orElseThrow(() -> new BaseException(DeliveryManagerErrorCode.DELIVERY_MANAGER_NOT_FOUND));

        if (!"MASTER".equals(role) && !"HUB_MANAGER".equals(role)) {
            if (!manager.getUserId().equals(UUID.fromString(requesterId))) {
                throw new BaseException(DeliveryManagerErrorCode.FORBIDDEN_ACCESS);
            }
        }

        return DeliveryManagerResponseDto.from(manager);
    }

    @Transactional
    public DeliveryManagerResponseDto updateDeliveryManager(UUID managerId, DeliveryManagerUpdateRequestDto request, String role) {
        checkAdminRole(role);

        DeliveryManager manager = deliveryManagerRepository.findById(managerId)
                .orElseThrow(() -> new BaseException(DeliveryManagerErrorCode.DELIVERY_MANAGER_NOT_FOUND));

        if (request.getHubId() != null) {
            boolean isHubActive = hubClient.checkHubActive(request.getHubId());
            if (!isHubActive) {
                throw new BaseException(DeliveryManagerErrorCode.HUB_IS_NOT_ACTIVE);
            }
            manager.updateHub(request.getHubId());
        }

        if (request.getType() != null) manager.updateType(request.getType());

        return DeliveryManagerResponseDto.from(manager);
    }

    @Transactional
    public void deleteDeliveryManager(UUID managerId, String requesterId, String role) {
        checkAdminRole(role);

        DeliveryManager manager = deliveryManagerRepository.findById(managerId)
                .orElseThrow(() -> new BaseException(DeliveryManagerErrorCode.DELIVERY_MANAGER_NOT_FOUND));

        manager.softDelete(UUID.fromString(requesterId));
    }


    @Transactional
    public void clearDeliveryManagerHubId(UUID hubId) {
        log.info("[DeliveryManagerHubClear] 허브 삭제에 따른 기사님 hubId 및 상태 일괄 초기화 시작. TargetHubId: {}", hubId);

        deliveryManagerRepository.clearHubIdAndChangeStatusByHubId(hubId, ManagerStatus.WAITING);

        log.info("[DeliveryManagerHubClear] 기사님 일괄 초기화 완료. TargetHubId: {}", hubId);
    }

    @Transactional(readOnly = true)
    public Page<DeliveryManagerResponseDto> getDeliveryManagerList(
            DeliveryManagerSearchDto searchDto,
            Pageable pageable,
            String role,
            String requesterHubId) {

        checkAdminRole(role);

        if ("HUB_MANAGER".equals(role)) {
            if (requesterHubId == null || requesterHubId.isBlank()) {
                log.error("[DeliveryManagerSearch] HUB_MANAGER의 요청에 hubId 헤더가 누락되었습니다.");
                throw new BaseException(DeliveryManagerErrorCode.FORBIDDEN_ACCESS);
            }
            searchDto.setHubId(UUID.fromString(requesterHubId));
        }

        Page<DeliveryManager> managerPage = deliveryManagerRepository.searchManagers(
                searchDto.getHubId(),
                searchDto.getType(),
                searchDto.getStatus(),
                pageable
        );

        return managerPage.map(DeliveryManagerResponseDto::from);
    }

    @Transactional
    public DeliveryAssignResponseDto assignNextDeliveryManager(DeliveryAssignRequestDto request) {
        DeliveryManager manager = deliveryManagerRepository
                .findFirstByHubIdAndTypeAndStatusAndDeletedAtIsNullOrderByLastAssignedAtAsc(
                        request.getHubId(), request.getType(), ManagerStatus.WAITING)
                .orElseThrow(() -> new BaseException(DeliveryManagerErrorCode.DELIVERY_MANAGER_NOT_FOUND));

        manager.recordAssignment();
        log.info("[DeliveryManagerAssign] 기사님 자동 배정 완료. ManagerId: {}, HubId: {}", manager.getId(), request.getHubId());

        // 임시 코드 (효승님과 조율 후 수정하겠습니다)
        DeliveryAssignedEvent event = DeliveryAssignedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("DeliveryAssigned")
                .occurredAt(java.time.ZonedDateTime.now().format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .version("1")
                // TODO: 현재 배달 서비스가 값을 안 주고 있으므로 임시 방어 코드 적용
                .deliveryId(request.getDeliveryId() != null ? request.getDeliveryId() : "임시-DELIVERY-ID")
                .order(mapToEventOrder(request.getOrder())) // 매핑 메서드 내부에서 null 체크 필수
                .route(mapToEventRoute(request.getRoute()))
                .agent(DeliveryAssignedEvent.AgentInfo.builder()
                        .agentId(manager.getUserId().toString())
                        .name("김배송")
                        .workingHours(DeliveryAssignedEvent.WorkingHours.builder()
                                .start("09:00").end("18:00").build())
                        .build())
                .build();

        notificationProducer.sendDeliveryAssignedEvent(event);

        return new DeliveryAssignResponseDto(manager.getId());
    }

    private DeliveryAssignedEvent.OrderInfo mapToEventOrder(DeliveryAssignRequestDto.OrderInfo orderDto) {
        if (orderDto == null) return null;
        List<DeliveryAssignedEvent.ProductInfo> products = orderDto.getProducts().stream()
                .map(p -> DeliveryAssignedEvent.ProductInfo.builder()
                        .name(p.getName())
                        .quantity(p.getQuantity())
                        .build())
                .toList();

        return DeliveryAssignedEvent.OrderInfo.builder()
                .orderId(orderDto.getOrderId())
                .ordererName(orderDto.getOrdererName())
                .orderedAt(orderDto.getOrderedAt())
                .products(products)
                .requesterNote(orderDto.getRequesterNote())
                .requestedDeadline(orderDto.getRequestedDeadline())
                .build();
    }

    private DeliveryAssignedEvent.RouteInfo mapToEventRoute(DeliveryAssignRequestDto.RouteInfo routeDto) {
        if (routeDto == null) return null;
        return DeliveryAssignedEvent.RouteInfo.builder()
                .origin(routeDto.getOrigin())
                .waypoints(routeDto.getWaypoints())
                .destination(routeDto.getDestination())
                .build();
    }


    private void checkAdminRole(String role) {
        if (!"MASTER".equals(role) && !"HUB_MANAGER".equals(role)) {
            throw new BaseException(DeliveryManagerErrorCode.FORBIDDEN_ACCESS);
        }
    }
}