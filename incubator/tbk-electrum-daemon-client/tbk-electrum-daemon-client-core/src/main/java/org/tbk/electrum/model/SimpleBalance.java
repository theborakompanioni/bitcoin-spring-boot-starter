package org.tbk.electrum.model;

import com.google.common.base.MoreObjects;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.tbk.electrum.command.BalanceResponse;

import javax.annotation.Nullable;
import java.util.Optional;

@Value
@Builder
public class SimpleBalance implements Balance {
    public static SimpleBalance from(BalanceResponse balance) {
        return SimpleBalance.builder()
                .confirmed(BtcTxoValues.fromBtcString(balance.getConfirmed()))
                .unconfirmed(balance.getUnconfirmed()
                        .map(BtcTxoValues::fromBtcString)
                        .orElseGet(SimpleTxoValue::zero))
                .unmatured(balance.getUnmatured()
                        .map(BtcTxoValues::fromBtcString)
                        .orElse(null))
                .lightning(balance.getLightning()
                        .map(BtcTxoValues::fromBtcString)
                        .orElse(null))
                .build();
    }

    private static final SimpleBalance ZERO = SimpleBalance.builder()
            .confirmed(SimpleTxoValue.zero())
            .unconfirmed(SimpleTxoValue.zero())
            .unmatured(SimpleTxoValue.zero())
            .lightning(SimpleTxoValue.zero())
            .build();

    public static SimpleBalance zero() {
        return ZERO;
    }

    @NonNull
    TxoValue confirmed;

    @Nullable
    TxoValue unconfirmed;

    @Nullable
    TxoValue unmatured;

    @Nullable
    TxoValue lightning;

    @Override
    public TxoValue getTotal() {
        return SimpleTxoValue.of(confirmed.getValue()
                                 + getUnconfirmed().getValue()
                                 + getUnmatured().getValue()
                                 + getLightning().getValue());
    }

    @Override
    public TxoValue getSpendable() {
        return SimpleTxoValue.of(confirmed.getValue() + unconfirmed.getValue());
    }

    @Override
    public TxoValue getUnconfirmed() {
        return Optional.ofNullable(unconfirmed)
                .orElseGet(SimpleTxoValue::zero);
    }

    @Override
    public TxoValue getUnmatured() {
        return Optional.ofNullable(unmatured)
                .orElseGet(SimpleTxoValue::zero);
    }

    @Override
    public TxoValue getLightning() {
        return Optional.ofNullable(lightning)
                .orElseGet(SimpleTxoValue::zero);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("total", getTotal().getValue())
                .add("spendable", getSpendable().getValue())
                .add("confirmed", confirmed.getValue())
                .add("unconfirmed", unconfirmed.getValue())
                .add("unmatured", getUnmatured().getValue())
                .add("unmatured", getUnmatured().getValue())
                .toString();
    }
}
