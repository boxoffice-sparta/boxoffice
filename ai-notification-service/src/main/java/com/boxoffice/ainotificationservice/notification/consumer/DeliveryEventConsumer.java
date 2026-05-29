package com.boxoffice.ainotificationservice.notification.consumer;

import com.boxoffice.ainotificationservice.notification.consumer.event.DeliveryStatusChangedEvent;
import com.boxoffice.ainotificationservice.notification.entity.message.EventCause;
import com.boxoffice.ainotificationservice.notification.entity.message.Recipient;
import com.boxoffice.ainotificationservice.notification.service.NotificationService;
import com.boxoffice.ainotificationservice.notification.template.DeliveryStatusContext;
import com.boxoffice.ainotificationservice.notification.template.TemplateContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// delivery.events 토픽 컨슈머. eventType 분기 → 멱등 처리 → 단일 채널 발송.
// DailyDeliveryScheduleAssigned(도전 기능)는 후속 작업이라 여기서 처리하지 않는다.
@Slf4j
@Component
public class DeliveryEventConsumer {

    private static final String GROUP = "ai-notification-service";
    private static final String SOURCE = "delivery-service";

    private final ObjectMapper objectMapper;
    private final IdempotentEventProcessor idempotentProcessor;
    private final NotificationService notificationService;
    private final String channelId;

    public DeliveryEventConsumer(
            ObjectMapper objectMapper,
            IdempotentEventProcessor idempotentProcessor,
            NotificationService notificationService,
            @Value("${notification.slack.channel-id}") String channelId) {
        this.objectMapper = objectMapper;
        this.idempotentProcessor = idempotentProcessor;
        this.notificationService = notificationService;
        this.channelId = channelId;
    }

    @KafkaListener(topics = "delivery.events", groupId = GROUP)
    public void consume(String message) {
        JsonNode node = readTree(message);
        String eventType = node.path("eventType").asText();
        switch (eventType) {
            case "DeliveryStatusChanged" -> {
                DeliveryStatusChangedEvent event = convert(node, DeliveryStatusChangedEvent.class);
                dispatch(event.eventId(), new DeliveryStatusContext(
                        event.deliveryId(), event.orderId(), event.status(),
                        event.recipientName(), event.failureReason()));
            }
            default -> log.warn("처리 대상이 아닌 delivery 이벤트 타입 - skip. eventType={}", eventType);
        }
    }

    private void dispatch(String eventId, TemplateContext context) {
        idempotentProcessor.processOnce(eventId, GROUP, () ->
                notificationService.sendFromEvent(
                        eventId, Recipient.channel(channelId), context, new EventCause(eventId, SOURCE)));
    }

    private JsonNode readTree(String message) {
        try {
            return objectMapper.readTree(message);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("delivery 이벤트 페이로드 파싱 실패", e);
        }
    }

    private <T> T convert(JsonNode node, Class<T> type) {
        try {
            return objectMapper.treeToValue(node, type);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("delivery 이벤트 역직렬화 실패: " + type.getSimpleName(), e);
        }
    }
}
