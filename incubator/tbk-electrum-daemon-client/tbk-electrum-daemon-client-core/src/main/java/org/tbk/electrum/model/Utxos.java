package org.tbk.electrum.model;

import java.util.List;

public interface Utxos {

    List<Utxo> getUtxos();

    boolean isEmpty();

    TxoValue getValue();
}
