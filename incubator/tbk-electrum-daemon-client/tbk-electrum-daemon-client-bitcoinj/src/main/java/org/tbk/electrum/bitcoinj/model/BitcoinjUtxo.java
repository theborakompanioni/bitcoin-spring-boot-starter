package org.tbk.electrum.bitcoinj.model;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;

public interface BitcoinjUtxo {

    Sha256Hash getTxHash();

    int getTxPos();

    long getHeight();

    Coin getValue();
}
