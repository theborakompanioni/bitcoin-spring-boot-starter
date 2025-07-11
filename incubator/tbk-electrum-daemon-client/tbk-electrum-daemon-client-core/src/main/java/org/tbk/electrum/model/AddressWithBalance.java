package org.tbk.electrum.model;

public interface AddressWithBalance {
    String getAddress();

    TxoValue getBalance();
}
