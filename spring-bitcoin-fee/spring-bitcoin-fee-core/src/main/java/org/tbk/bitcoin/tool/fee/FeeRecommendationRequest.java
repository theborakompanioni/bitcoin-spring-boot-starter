package org.tbk.bitcoin.tool.fee;

import org.tbk.bitcoin.tool.fee.util.MoreBitcoin;

import java.time.Duration;
import java.util.Optional;

public interface FeeRecommendationRequest {

    Optional<Duration> getDurationTarget();

    Optional<Confidence> getDesiredConfidence();

    default Optional<Long> getBlockTarget() {
        return getDurationTarget().map(val -> val.dividedBy(MoreBitcoin.averageBlockDuration()));
    }

    interface Confidence {
        double getConfidenceValue();
    }
}
