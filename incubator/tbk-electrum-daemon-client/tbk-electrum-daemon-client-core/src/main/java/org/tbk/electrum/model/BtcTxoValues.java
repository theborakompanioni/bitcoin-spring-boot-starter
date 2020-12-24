package org.tbk.electrum.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public final class BtcTxoValues {
    private static final long ONE_BTC_IN_SATOSHI = 100_000_000L;

    public static TxoValue fromBtcString(@Nonnull String valueAsBtcString) {
        return SimpleTxoValue.of(new BigDecimal(valueAsBtcString)
                .setScale(8, RoundingMode.UNNECESSARY)
                .multiply(BigDecimal.valueOf(ONE_BTC_IN_SATOSHI))
                .longValue());
    }

    public static TxoValue fromBtcStringOrZero(@Nullable String btcStringOrNull) {
        return Optional.ofNullable(btcStringOrNull)
                .map(BtcTxoValues::fromBtcString)
                .orElseGet(SimpleTxoValue::zero);
    }

    public static BigDecimal toBtc(TxoValue value) {
        return BigDecimal.valueOf(value.getValue())
                .setScale(8, RoundingMode.UNNECESSARY)
                .divide(BigDecimal.valueOf(ONE_BTC_IN_SATOSHI), RoundingMode.UNNECESSARY);
    }

    private BtcTxoValues() {
        throw new UnsupportedOperationException();
    }
}
