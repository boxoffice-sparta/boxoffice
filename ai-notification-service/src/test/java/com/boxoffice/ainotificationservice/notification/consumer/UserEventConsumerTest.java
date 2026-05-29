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
import com.boxoffice.ainotificationservice.notification.template.MasterSignupRequestContext;
import com.boxoffice.ainotificationservice.notification.template.UserApprovedContext;
import com.boxoffice.ainotificationservice.notification.template.UserRejectedContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("UserEventConsumer")
@ExtendWith(MockitoExtension.class)
class UserEventConsumerTest {

    private static final String CHANNEL = "C-TEST";

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private NotificationService notificationService;

    private UserEventConsumer consumer;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        IdempotentEventProcessor idempotentProcessor = new IdempotentEventProcessor(processedEventRepository);
        consumer = new UserEventConsumer(objectMapper, idempotentProcessor, notificationService, CHANNEL);
    }

    @Test
    @DisplayName("UserApproved - 단일 채널로 UserApprovedContext 발송")
    void user_approved() {
        // given
        given(processedEventRepository.existsByEventIdAndConsumerGroup(any(), any())).willReturn(false);
        String message = """
                {"eventType":"UserApproved","eventId":"evt-1","userId":"u-1","userName":"홍길동"}
                """;

        // when
        consumer.consume(message);

        // then
        then(notificationService).should().sendFromEvent(
                eq("evt-1"),
                eq(Recipient.channel(CHANNEL)),
                eq(new UserApprovedContext("홍길동")),
                eq(new EventCause("evt-1", "user-service")));
    }

    @Test
    @DisplayName("UserSignupRequested - MasterSignupRequestContext 발송")
    void user_signup_requested() {
        // given
        given(processedEventRepository.existsByEventIdAndConsumerGroup(any(), any())).willReturn(false);
        String message = """
                {"eventType":"UserSignupRequested","eventId":"evt-2",
                 "applicantName":"김지원","email":"jiwon@example.com","requestedRole":"MASTER"}
                """;

        // when
        consumer.consume(message);

        // then
        then(notificationService).should().sendFromEvent(
                eq("evt-2"),
                eq(Recipient.channel(CHANNEL)),
                eq(new MasterSignupRequestContext("김지원", "jiwon@example.com", "MASTER")),
                eq(new EventCause("evt-2", "user-service")));
    }

    @Test
    @DisplayName("UserRejected - UserRejectedContext 발송")
    void user_rejected() {
        // given
        given(processedEventRepository.existsByEventIdAndConsumerGroup(any(), any())).willReturn(false);
        String message = """
                {"eventType":"UserRejected","eventId":"evt-3","userName":"이서준","reason":"서류 미비"}
                """;

        // when
        consumer.consume(message);

        // then
        then(notificationService).should().sendFromEvent(
                eq("evt-3"),
                eq(Recipient.channel(CHANNEL)),
                eq(new UserRejectedContext("이서준", "서류 미비")),
                eq(new EventCause("evt-3", "user-service")));
    }

    @Test
    @DisplayName("알 수 없는 eventType - 발송하지 않고 skip")
    void unknown_event_type_skipped() {
        // given
        String message = """
                {"eventType":"UserDeleted","eventId":"evt-9"}
                """;

        // when
        consumer.consume(message);

        // then
        then(notificationService).should(never()).sendFromEvent(any(), any(), any(), any());
    }
}
