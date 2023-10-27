package org.tbk.bitcoin.tool.fee;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Value
@Builder
public class FeeRecommendationResponseImpl implements FeeRecommendationResponse {

    @Singular("addFeeRecommendation")
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "immutable list")
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
        private BigDecimal satPerVbyteValue;

        private SatPerVbyteImpl(BigDecimal satPerVbyteValue) {
            requireNonNull(satPerVbyteValue);
            checkArgument(satPerVbyteValue.compareTo(BigDecimal.ZERO) >= 0);

            this.satPerVbyteValue = satPerVbyteValue;
        }

        @Override
        public BigDecimal getValue() {
            return satPerVbyteValue;
        }

        @Override
        public String toString() {
            if (BigDecimal.ONE.compareTo(satPerVbyteValue) == 0) {
                return "1 sat/vByte";
            }
            return "%s sats/vByte".formatted(satPerVbyteValue.setScale(2, RoundingMode.UP).toPlainString());
        }
    }
}
