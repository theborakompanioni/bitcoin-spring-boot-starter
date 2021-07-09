package org.tbk.bitcoin.tool.fee;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Value
@Builder
public class FeeRecommendationResponseImpl implements FeeRecommendationResponse {

    @Singular("addFeeRecommendation")
    List<FeeRecommendation> feeRecommendations;

    ProviderInfo providerInfo;

    @Value
    @Builder
    public static class FeeRecommendationImpl implements FeeRecommendation {
        @NonNull
        FeeRecommendationResponse.FeeUnit feeUnit;
    }

    @Builder
    public static final class SatPerVbyteImpl implements FeeUnit {
        @NonNull
        BigDecimal satPerVbyteValue;

        private SatPerVbyteImpl(BigDecimal satPerVbyteValue) {
            requireNonNull(satPerVbyteValue);
            checkArgument(satPerVbyteValue.compareTo(BigDecimal.ZERO) >= 0);

            this.satPerVbyteValue = satPerVbyteValue;
        }

        public BigDecimal getValue() {
            return satPerVbyteValue;
        }
    }
}
