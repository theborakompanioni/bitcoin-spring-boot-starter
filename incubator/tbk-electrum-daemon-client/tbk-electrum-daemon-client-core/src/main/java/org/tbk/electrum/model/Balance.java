package org.tbk.electrum.model;

public interface Balance {
    TxoValue getConfirmed();

    TxoValue getUnconfirmed();

    TxoValue getUnmatured();

    TxoValue getLightning();

    TxoValue getTotal();

    TxoValue getSpendable();
}
