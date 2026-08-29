package org.tbk.electrum.bitcoinj.model;

import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Sha256Hash;

public interface BitcoinjUtxo {

    Sha256Hash getTxHash();

    int getTxPos();

    long getHeight();

    Coin getValue();
}
