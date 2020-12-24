package org.tbk.electrum.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SimpleUtxo implements Utxo {

    long height;

    @NonNull
    String txHash;

    int txPos;

    @NonNull
    TxoValue value;
}
