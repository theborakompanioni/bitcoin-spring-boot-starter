package org.tbk.electrum.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SimpleOnchainSummary implements OnchainSummary {
    @NonNull
    TxoValue startBalance;

    @NonNull
    TxoValue endBalance;

    @NonNull
    TxoValue incoming;

    @NonNull
    TxoValue outgoing;
}
