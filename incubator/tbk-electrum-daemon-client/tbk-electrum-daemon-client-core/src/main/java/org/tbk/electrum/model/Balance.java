package org.tbk.electrum.model;

public interface Balance {
    TxoValue getConfirmed();

    TxoValue getUnconfirmed();

    TxoValue getTotal();
}
