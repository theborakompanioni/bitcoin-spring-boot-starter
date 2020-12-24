package org.tbk.electrum.model;

public interface Utxo {

    String getTxHash();

    int getTxPos();

    long getHeight();

    TxoValue getValue();
}
