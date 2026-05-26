package com.boxoffice.ainotificationservice.ai.client;

import com.boxoffice.ainotificationservice.ai.deadline.DispatchDeadlineInput;
import com.boxoffice.ainotificationservice.ai.deadline.DispatchDeadlinePrediction;
import com.boxoffice.ainotificationservice.ai.deadline.WorkingHours;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FakeLlmClient")
class FakeLlmClientTest {

    private static final DispatchDeadlineInput INPUT = new DispatchDeadlineInput(
            LocalDateTime.of(2026, 5, 21, 18, 0),
            Duration.ofMinutes(120),
            WorkingHours.defaultHours()
    );

    @Nested
    @DisplayName("기본 전략")
    class DefaultStrategy {

        @Test
        @DisplayName("성공 - 납기 - 이동시간 - 30분 으로 예측, fallbackUsed false")
        void success_default_prediction() {
            // given
            FakeLlmClient client = new FakeLlmClient();

            // when
            DispatchDeadlinePrediction prediction = client.predictDispatchDeadline(INPUT);

            // then
            assertThat(prediction.dispatchDeadline())
                    .isEqualTo(LocalDateTime.of(2026, 5, 21, 15, 30));
            assertThat(prediction.fallbackUsed()).isFalse();
            assertThat(prediction.confidenceOptional()).contains(0.8);
            assertThat(client.recordedInputs()).containsExactly(INPUT);
        }
    }

    @Nested
    @DisplayName("주입된 전략")
    class CustomStrategy {

        @Test
        @DisplayName("전략에서 fallback prediction 반환 가능")
        void custom_strategy_fallback() {
            // given
            FakeLlmClient client = new FakeLlmClient(
                    input -> DispatchDeadlinePrediction.fallback(LocalDateTime.of(2026, 5, 21, 14, 0))
            );

            // when
            DispatchDeadlinePrediction prediction = client.predictDispatchDeadline(INPUT);

            // then
            assertThat(prediction.fallbackUsed()).isTrue();
            assertThat(prediction.confidenceOptional()).isEmpty();
        }
    }
}
