package org.tbk.electrum.model;

public interface Balance {
    TxoValue getConfirmed();

    TxoValue getUnconfirmed();

    TxoValue getUnmatured();

    TxoValue getTotal();

    TxoValue getSpendable();
}
