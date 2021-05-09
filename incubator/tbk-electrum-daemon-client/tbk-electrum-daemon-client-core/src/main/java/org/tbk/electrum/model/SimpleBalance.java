package org.tbk.electrum.model;

import com.google.common.base.MoreObjects;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.Nullable;
import java.util.Optional;

@Value
@Builder
public class SimpleBalance implements Balance {
    private static SimpleBalance ZERO = SimpleBalance.builder()
            .confirmed(SimpleTxoValue.zero())
            .unconfirmed(SimpleTxoValue.zero())
            .unmatured(SimpleTxoValue.zero())
            .build();

    public static SimpleBalance zero() {
        return ZERO;
    }

    @NonNull
    TxoValue confirmed;

    @NonNull
    TxoValue unconfirmed;

    @Nullable
    TxoValue unmatured;

    @Override
    public TxoValue getTotal() {
        return SimpleTxoValue.of(confirmed.getValue() +
                unconfirmed.getValue() +
                getUnmatured().getValue());
    }

    @Override
    public TxoValue getSpendable() {
        return SimpleTxoValue.of(confirmed.getValue() + unconfirmed.getValue());
    }

    @Override
    public TxoValue getUnmatured() {
        return Optional.ofNullable(unmatured)
                .orElseGet(SimpleTxoValue::zero);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("total", getTotal().getValue())
                .add("spendable", getSpendable().getValue())
                .add("confirmed", confirmed.getValue())
                .add("unconfirmed", unconfirmed.getValue())
                .add("unmatured", getUnmatured().getValue())
                .toString();
    }
}
