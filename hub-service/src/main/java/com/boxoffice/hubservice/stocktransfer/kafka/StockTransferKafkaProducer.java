package com.boxoffice.hubservice.stocktransfer.kafka;

import com.boxoffice.hubservice.stocktransfer.event.TransferDispatchedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StockTransferKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendDispatched(UUID transferId, UUID fromHubId, UUID toHubId) {
        kafkaTemplate.send("hub.transfer.dispatched",
                new TransferDispatchedEvent(transferId, fromHubId, toHubId));
    }
}