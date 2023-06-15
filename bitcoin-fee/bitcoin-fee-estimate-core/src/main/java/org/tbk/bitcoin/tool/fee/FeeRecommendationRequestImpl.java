package org.tbk.bitcoin.tool.fee;

import lombok.*;

import java.time.Duration;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

@Value
@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FeeRecommendationRequestImpl implements FeeRecommendationRequest {

    @Value
    public static class ConfidenceImpl implements Confidence {
        double confidenceValue;

        @Builder
        public ConfidenceImpl(double confidenceValue) {
            // we can never have a hundred percent guarantee that we make it into the next block
            checkArgument(confidenceValue < 1d, "value must be lower than 1");
            //  we cannot guarantee the other way around either: this means "I really don't care" and should
            // therefore be expressed with "durationTarget"
            checkArgument(confidenceValue > 0d, "value must be greater than zero");

            this.confidenceValue = confidenceValue;
        }
    }

    @NonNull
    Duration durationTarget;

    Confidence desiredConfidence;

    @Override
    public Optional<Confidence> getDesiredConfidence() {
        return Optional.ofNullable(desiredConfidence);
    }
}
