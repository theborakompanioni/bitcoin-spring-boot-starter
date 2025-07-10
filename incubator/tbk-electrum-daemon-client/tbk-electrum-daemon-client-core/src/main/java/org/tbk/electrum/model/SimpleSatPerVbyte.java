package org.tbk.electrum.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class SimpleSatPerVbyte implements SatPerVbyte {

    @NonNull
    BigDecimal satPerVbyte;

    @Override
    public BigDecimal getSatPerKvbyte() {
        return satPerVbyte.multiply(BigDecimal.valueOf(1_000));
    }
}
