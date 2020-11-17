package org.tbk.bitcoin.tool.fee;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@Value
@Builder
public class FeeRecommendationResponseImpl implements FeeRecommendationResponse {

    @Singular("addFeeRecommendation")
    List<FeeRecommendation> feeRecommendations;

    @Value
    @Builder
    public static class FeeRecommendationImpl implements FeeRecommendation {
        @NonNull
        SatPerVbyte satPerVbyte;
    }

    @Value
    @Builder
    public static class SatPerVbyteImpl implements SatPerVbyte {
        long satPerVbyteValue;

        private SatPerVbyteImpl(long satPerVbyteValue) {
            checkArgument(satPerVbyteValue >= 0L);
            this.satPerVbyteValue = satPerVbyteValue;
        }
    }
}
