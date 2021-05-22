package org.tbk.electrum.bitcoinj.model;

import org.bitcoinj.core.Coin;

import java.util.List;

public interface BitcoinjUtxos {

    List<BitcoinjUtxo> getUtxos();

    boolean isEmpty();

    Coin getValue();
}
