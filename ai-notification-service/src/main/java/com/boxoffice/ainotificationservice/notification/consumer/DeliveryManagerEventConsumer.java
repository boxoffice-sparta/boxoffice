package com.boxoffice.ainotificationservice.notification.consumer;

import com.boxoffice.ainotificationservice.ai.deadline.DeliveryRoute;
import com.boxoffice.ainotificationservice.ai.deadline.DispatchDeadlineContext;
import com.boxoffice.ainotificationservice.ai.deadline.DispatchDeadlinePrediction;
import com.boxoffice.ainotificationservice.ai.deadline.OrderLine;
import com.boxoffice.ainotificationservice.ai.deadline.WorkingHours;
import com.boxoffice.ainotificationservice.ai.service.DispatchDeadlinePredictor;
import com.boxoffice.ainotificationservice.notification.consumer.event.DeliveryAssignedEvent;
import com.boxoffice.ainotificationservice.notification.consumer.event.DeliveryManagerEvent;
import com.boxoffice.ainotificationservice.notification.entity.message.EventCause;
import com.boxoffice.ainotificationservice.notification.entity.message.Recipient;
import com.boxoffice.ainotificationservice.notification.service.NotificationService;
import com.boxoffice.ainotificationservice.notification.template.DispatchDeadlineNotificationContext;
import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// delivery-manager.events 토픽 컨슈머. DeliveryAssigned 수신 → AI 발송 시한 예측 → 단일 채널 발송.
@Component
public class DeliveryManagerEventConsumer {

    private static final String GROUP = "ai-notification-service";
    private static final String SOURCE = "delivery-manager-service";

    private final IdempotentEventProcessor idempotentProcessor;
    private final NotificationService notificationService;
    private final DispatchDeadlinePredictor predictor;
    private final String channelId;

    public DeliveryManagerEventConsumer(
            IdempotentEventProcessor idempotentProcessor,
            NotificationService notificationService,
            DispatchDeadlinePredictor predictor,
            @Value("${notification.slack.channel-id}") String channelId) {
        this.idempotentProcessor = idempotentProcessor;
        this.notificationService = notificationService;
        this.predictor = predictor;
        this.channelId = channelId;
    }

    @KafkaListener(topics = "delivery-manager.events", groupId = GROUP)
    public void consume(DeliveryManagerEvent event) {
        switch (event) {
            case DeliveryAssignedEvent e -> handleDeliveryAssigned(e);
        }
    }

    private void handleDeliveryAssigned(DeliveryAssignedEvent event) {
        // 무거운 예측은 멱등/트랜잭션 경계 밖에서 수행. 중복 이벤트는 동일 입력이라 캐시 히트로 저렴.
        DispatchDeadlinePrediction prediction = predictor.predict(toPredictionContext(event));
        DispatchDeadlineNotificationContext context = new DispatchDeadlineNotificationContext(
                event.agent().name(),
                event.order().orderId(),
                prediction.dispatchDeadline(),
                prediction.reasoning());
        idempotentProcessor.processOnce(event.eventId(), GROUP, () ->
                notificationService.sendFromEvent(
                        event.eventId(), Recipient.channel(channelId), context,
                        new EventCause(event.eventId(), SOURCE)));
    }

    private DispatchDeadlineContext toPredictionContext(DeliveryAssignedEvent event) {
        DeliveryAssignedEvent.Order order = event.order();
        List<OrderLine> products = order.products().stream()
                .map(p -> new OrderLine(p.name(), p.quantity()))
                .toList();
        DeliveryRoute route = new DeliveryRoute(
                event.route().origin(), event.route().waypoints(), event.route().destination());
        WorkingHours workingHours = new WorkingHours(
                LocalTime.parse(event.agent().workingHours().start()),
                LocalTime.parse(event.agent().workingHours().end()));
        return new DispatchDeadlineContext(
                OffsetDateTime.parse(order.requestedDeadline()).toLocalDateTime(),
                order.requesterNote(),
                products,
                route,
                Duration.ofSeconds(event.totalEstimatedDurationSeconds()),
                workingHours);
    }
}
