package org.tbk.electrum.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

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

    @NonNull
    TxoValue unmatured;

    @Override
    public TxoValue getTotal() {
        return SimpleTxoValue.of(confirmed.getValue() + unconfirmed.getValue());
    }
}
