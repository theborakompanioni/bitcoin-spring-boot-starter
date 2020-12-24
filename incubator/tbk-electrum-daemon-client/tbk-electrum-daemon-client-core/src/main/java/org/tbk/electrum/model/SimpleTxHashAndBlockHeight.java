package org.tbk.electrum.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SimpleTxHashAndBlockHeight implements TxHashAndBlockHeight {

    @NonNull
    String txHash;

    long height;

}
