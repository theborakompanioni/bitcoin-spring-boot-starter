package org.tbk.bitcoin.tool.fee;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.time.Duration;
import java.util.Optional;

@Value
@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FeeRecommendationRequestImpl implements FeeRecommendationRequest {
    @Value
    @Builder
    public static class ConfidenceImpl implements Confidence {
        double confidenceValue;
    }

    Duration durationTarget;
    Confidence desiredConfidence;

    @Override
    public Optional<Duration> getDurationTarget() {
        return Optional.ofNullable(durationTarget);
    }

    @Override
    public Optional<Confidence> getDesiredConfidence() {
        return Optional.ofNullable(desiredConfidence);
    }
}
