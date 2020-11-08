package org.tbk.bitcoin.txstats.example.score.label;

import com.google.common.base.MoreObjects;
import lombok.*;

public interface ScoreLabel {

    String getName();

    default String getDescription() {
        return "";
    }

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class SimpleScoreLabel implements ScoreLabel {
        @NonNull
        String name;

        @NonNull
        String description;


        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .addValue(name)
                    .toString();
        }
    }
}
