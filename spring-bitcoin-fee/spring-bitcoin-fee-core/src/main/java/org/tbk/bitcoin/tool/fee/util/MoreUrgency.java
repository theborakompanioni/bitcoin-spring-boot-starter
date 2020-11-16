package org.tbk.bitcoin.tool.fee.util;

import com.google.common.collect.ImmutableMap;
import org.tbk.bitcoin.tool.fee.FeeRecommendationRequest.Urgency;

import java.time.Duration;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

public final class MoreUrgency {

    private MoreUrgency() {
        throw new UnsupportedOperationException();
    }

    // should provide "sane" default mapping from urgency to duration
    private static final Map<Urgency, Duration> urgencyToDurationMapping = ImmutableMap.<Urgency, Duration>builder()
            .put(Urgency.MIN, Duration.ofDays(7))
            .put(Urgency.LOW, Duration.ofHours(24))
            .put(Urgency.MEDIUM, Duration.ofHours(2))
            .put(Urgency.UNRECOGNIZED, Duration.ofHours(1))
            .put(Urgency.DEFAULT, Duration.ofHours(1))
            .put(Urgency.HIGH, Duration.ofMinutes(30))
            .put(Urgency.HAX, Duration.ofMinutes(10L))
            .build();

    // NOTE: add reverse mapping durationToUrgency when (if!) needed
    public static Duration urgencyToDuration(Urgency urgency) {
        return urgencyToDurationMapping.get(urgency);
    }

    public static long urgencyToBlockNum(Urgency urgency) {
        return urgencyToDurationMapping.get(urgency)
                .dividedBy(MoreBitcoin.averageBlockDuration());
    }

    public static Urgency blockNumToUrgency(long blockNum) {
        Duration duration = MoreBitcoin.averageBlockDuration().multipliedBy(blockNum);

        Optional<Urgency> urgencyOrEmpty = urgencyToDurationMapping.entrySet().stream()
                .sorted(Comparator.comparingLong(val -> val.getValue().toSeconds()))
                .filter(val -> !val.getValue().minus(duration).isNegative())
                .map(val -> val.getKey())
                .findFirst();

        return urgencyOrEmpty.orElse(Urgency.MIN);
    }
}
