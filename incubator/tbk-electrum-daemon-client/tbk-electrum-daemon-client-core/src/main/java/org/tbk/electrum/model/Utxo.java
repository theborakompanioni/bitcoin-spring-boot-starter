package org.tbk.electrum.model;

import java.util.Optional;

public interface Utxo {

    String getTxHash();

    int getTxPos();

    long getHeight();

    TxoValue getValue();

    Optional<String> getAddress();
}
