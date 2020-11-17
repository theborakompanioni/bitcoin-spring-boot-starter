package org.tbk.bitcoin.tool.fee;

import java.time.Duration;
import java.util.Optional;

public interface FeeRecommendationRequest {

    Optional<Duration> getDurationTarget();

    Optional<Confidence> getDesiredConfidence();

    interface Confidence {
        double getConfidenceValue();
    }
}
