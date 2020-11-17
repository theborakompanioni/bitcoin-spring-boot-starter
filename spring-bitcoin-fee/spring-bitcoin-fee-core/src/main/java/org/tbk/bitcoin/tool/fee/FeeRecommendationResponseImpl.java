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

        public static SatPerVbyteImpl fromSatPerWeightUnit(long setPerWaitUnit) {
            return SatPerVbyteImpl.builder()
                    .satPerVbyteValue(setPerWaitUnit / 4)
                    .build();
        }

        public static SatPerVbyteImpl fromSatPerKilobyte(long satPerKilobyte) {
            return fromSatPerByte(satPerKilobyte / 1_000);
        }

        public static SatPerVbyteImpl fromSatPerByte(long satPerByte) {
            // TODO: this is not right atm
            // e.g. https://bitcoin.stackexchange.com/questions/84004/how-do-virtual-size-stripped-size-and-raw-size-compare-between-legacy-address-f/84006#84006
            // bytes and vbytes are the same for legacy addresses but not for tx with witnesses
            return SatPerVbyteImpl.builder()
                    .satPerVbyteValue((long) (satPerByte * 1.4d))
                    .build();
        }

        long satPerVbyteValue;

        private SatPerVbyteImpl(long satPerVbyteValue) {
            checkArgument(satPerVbyteValue >= 0L);
            this.satPerVbyteValue = satPerVbyteValue;
        }
    }
}
