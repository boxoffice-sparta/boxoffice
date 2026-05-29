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
import com.boxoffice.ainotificationservice.notification.template.OrderCanceledContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("OrderEventConsumer")
@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    private static final String CHANNEL = "C-TEST";

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private NotificationService notificationService;

    private OrderEventConsumer consumer;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        IdempotentEventProcessor idempotentProcessor = new IdempotentEventProcessor(processedEventRepository);
        consumer = new OrderEventConsumer(objectMapper, idempotentProcessor, notificationService, CHANNEL);
    }

    @Test
    @DisplayName("OrderCanceled - 단일 채널로 OrderCanceledContext 발송")
    void order_canceled() {
        // given
        given(processedEventRepository.existsByEventIdAndConsumerGroup(any(), any())).willReturn(false);
        String message = """
                {"eventType":"OrderCanceled","eventId":"evt-1","orderId":"ORD-100","reason":"재고 부족",
                 "ordererName":"홍길동","hubManagerName":"김허브"}
                """;

        // when
        consumer.consume(message);

        // then
        then(notificationService).should().sendFromEvent(
                eq("evt-1"),
                eq(Recipient.channel(CHANNEL)),
                eq(new OrderCanceledContext("ORD-100", "재고 부족", "홍길동", "김허브")),
                eq(new EventCause("evt-1", "order-service")));
    }

    @Test
    @DisplayName("알 수 없는 eventType - 발송하지 않고 skip")
    void unknown_event_type_skipped() {
        // given
        String message = """
                {"eventType":"OrderCreated","eventId":"evt-9"}
                """;

        // when
        consumer.consume(message);

        // then
        then(notificationService).should(never()).sendFromEvent(any(), any(), any(), any());
    }
}
