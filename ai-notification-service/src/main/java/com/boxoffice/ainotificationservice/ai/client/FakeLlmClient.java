package com.boxoffice.ainotificationservice.ai.client;

import com.boxoffice.ainotificationservice.ai.deadline.DispatchDeadlineInput;
import com.boxoffice.ainotificationservice.ai.deadline.DispatchDeadlinePrediction;
import com.boxoffice.common.exception.BaseException;
import com.boxoffice.common.exception.CommonErrorCode;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

// 실제 LLM 어댑터 도입 전 placeholder. 기본 전략은 결정론적 테스트용 임의값.
public class FakeLlmClient implements LlmClient {

    private static final Duration DEFAULT_MARGIN = Duration.ofMinutes(30);
    private static final String DEFAULT_REASONING = "fake-llm: requestedDeadline - totalEstimatedDuration - 30m";
    private static final Double DEFAULT_CONFIDENCE = 0.8;

    private final List<DispatchDeadlineInput> inputHistory = new ArrayList<>();
    private final Function<DispatchDeadlineInput, DispatchDeadlinePrediction> strategy;

    public FakeLlmClient() {
        this(FakeLlmClient::defaultStrategy);
    }

    public FakeLlmClient(Function<DispatchDeadlineInput, DispatchDeadlinePrediction> strategy) {
        if (strategy == null) {
            throw new BaseException(CommonErrorCode.INVALID_INPUT);
        }
        this.strategy = strategy;
    }

    @Override
    public DispatchDeadlinePrediction predictDispatchDeadline(DispatchDeadlineInput input) {
        inputHistory.add(input);
        return strategy.apply(input);
    }

    public List<DispatchDeadlineInput> recordedInputs() {
        return List.copyOf(inputHistory);
    }

    private static DispatchDeadlinePrediction defaultStrategy(DispatchDeadlineInput input) {
        LocalDateTime deadline = input.requestedDeadline()
                .minus(input.totalEstimatedDuration())
                .minus(DEFAULT_MARGIN);
        return DispatchDeadlinePrediction.llm(deadline, DEFAULT_REASONING, DEFAULT_CONFIDENCE);
    }
}
