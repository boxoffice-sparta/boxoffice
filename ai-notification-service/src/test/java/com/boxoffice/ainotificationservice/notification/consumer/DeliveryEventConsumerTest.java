package com.boxoffice.ainotificationservice.notification.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.boxoffice.ainotificationservice.notification.entity.message.EventCause;
import com.boxoffice.ainotificationservice.notification.entity.message.Recipient;
import com.boxoffice.ainotificationservice.notification.repository.ProcessedEventRepository;
import com.boxoffice.ainotificationservice.notification.service.NotificationService;
import com.boxoffice.ainotificationservice.notification.template.DeliveryStatus;
import com.boxoffice.ainotificationservice.notification.template.DeliveryStatusContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("DeliveryEventConsumer")
@ExtendWith(MockitoExtension.class)
class DeliveryEventConsumerTest {

    private static final String CHANNEL = "C-TEST";

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private NotificationService notificationService;

    private DeliveryEventConsumer consumer;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        IdempotentEventProcessor idempotentProcessor = new IdempotentEventProcessor(processedEventRepository);
        consumer = new DeliveryEventConsumer(objectMapper, idempotentProcessor, notificationService, CHANNEL);
    }

    @Test
    @DisplayName("DeliveryStatusChanged(FAILED) - DeliveryStatusContext 발송")
    void delivery_status_failed() {
        // given
        given(processedEventRepository.existsByEventIdAndConsumerGroup(any(), any())).willReturn(false);
        String message = """
                {"eventType":"DeliveryStatusChanged","eventId":"evt-1","deliveryId":"DLV-1","orderId":"ORD-1",
                 "status":"FAILED","failureReason":"주소 불명"}
                """;

        // when
        consumer.consume(message);

        // then
        then(notificationService).should().sendFromEvent(
                eq("evt-1"),
                eq(Recipient.channel(CHANNEL)),
                eq(new DeliveryStatusContext("DLV-1", "ORD-1", DeliveryStatus.FAILED, null, "주소 불명")),
                eq(new EventCause("evt-1", "delivery-service")));
    }

    @Test
    @DisplayName("DailyDeliveryScheduleAssigned(후순위) - 발송하지 않고 skip")
    void daily_schedule_skipped() {
        // given
        String message = """
                {"eventType":"DailyDeliveryScheduleAssigned","eventId":"evt-9"}
                """;

        // when
        consumer.consume(message);

        // then
        then(notificationService).should(never()).sendFromEvent(any(), any(), any(), any());
    }
}
