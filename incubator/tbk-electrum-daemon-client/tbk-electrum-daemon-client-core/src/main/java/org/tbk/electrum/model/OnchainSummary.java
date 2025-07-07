package org.tbk.electrum.model;

public interface OnchainSummary {
    TxoValue getStartBalance();

    TxoValue getEndBalance();

    TxoValue getIncoming();

    TxoValue getOutgoing();
}
