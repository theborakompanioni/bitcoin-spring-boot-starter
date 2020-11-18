package org.tbk.bitcoin.tool.fee;

import lombok.*;

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

    @NonNull
    Duration durationTarget;

    Confidence desiredConfidence;

    @Override
    public Optional<Confidence> getDesiredConfidence() {
        return Optional.ofNullable(desiredConfidence);
    }
}
