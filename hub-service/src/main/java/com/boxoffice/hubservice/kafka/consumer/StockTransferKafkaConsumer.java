package com.boxoffice.hubservice.kafka.consumer;

import com.boxoffice.common.exception.BaseException;
import com.boxoffice.hubservice.domain.hub.exception.HubErrorCode;
import com.boxoffice.hubservice.domain.stocktransfer.entity.StockTransfer;
import com.boxoffice.hubservice.domain.stocktransfer.repository.StockTransferRepository;
import com.boxoffice.hubservice.kafka.event.AssignmentFailedEvent;
import com.boxoffice.hubservice.kafka.event.AssignmentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockTransferKafkaConsumer {

    private final StockTransferRepository stockTransferRepository;

    @KafkaListener(topics = "transfer-assign-success", groupId = "hub-service")
    @Transactional
    public void onAssigned(AssignmentSucceededEvent event) {
        StockTransfer transfer = stockTransferRepository.findById(event.transferId())
                .orElseThrow(() -> new BaseException(HubErrorCode.TRANSFER_NOT_FOUND));
        transfer.assignDeliveryManager(event.deliveryManagerId());
        log.info("Stock transfer {} assigned to delivery manager {}", event.transferId(), event.deliveryManagerId());
    }

    @KafkaListener(topics = "transfer-assign-failed", groupId = "hub-service")
    @Transactional
    public void onFailed(AssignmentFailedEvent event) {
        StockTransfer transfer = stockTransferRepository.findById(event.transferId())
                .orElseThrow(() -> new BaseException(HubErrorCode.TRANSFER_NOT_FOUND));
        transfer.revertDispatch();
        log.warn("Stock transfer {} assignment failed, reverted to PENDING. Reason: {}",
                event.transferId(), event.reason());
    }
}
