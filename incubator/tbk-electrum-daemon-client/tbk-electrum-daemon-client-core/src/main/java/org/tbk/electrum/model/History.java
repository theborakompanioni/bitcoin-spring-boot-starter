package org.tbk.electrum.model;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface History {
    Summary getSummary();

    List<Transaction> getTransactions();

    interface Summary {
        TxoValue getStartBalance();

        TxoValue getEndBalance();

        TxoValue getIncoming();

        TxoValue getOutgoing();
    }

    interface Transaction {
        String getTxHash();

        /**
         * Positive value for incoming transaction.
         * Negative value for outgoing transaction.
         *
         * @return the value of the transaction from the wallets points of view.
         */
        TxoValue getValue();

        /**
         * The wallet balance after the transaction takes effect.
         *
         * <p>This can also be negative!
         * If a utxo is spent in the same block and electrum sees the outgoing before
         * the incoming transaction first, then the returned `balance` of the wallet
         * might - for a very short time - be below zero!
         *
         * <p>e.g.:
         * 1. "SimpleHistory.SimpleTransaction(
         * balance=SimpleTxoValue(value=-100000),
         * value=SimpleTxoValue(value=-100000),
         * txHash=3076c967128846e2782b8bd337f350e6aecbac98fbaac9df633e25c25fc42e01,
         * height=Optional[583138],
         * incoming=false,
         * ...
         * )"
         * 2. "SimpleHistory.SimpleTransaction(
         * balance=SimpleTxoValue(value=0),
         * value=SimpleTxoValue(value=100000),
         * txHash=f0bc9fc42b984d34b863f89cd0edb982851d2563764feddf74787a9c9e9235d9,
         * height=Optional[583138],
         * incoming=true, ...
         * )"
         *
         * <p>NOTE: This seems to be reordered once electrum has fetched all timestamps.
         * So, this should only happen, when electrum needs to do some syncing.
         * After all transactions have timestamps this effect seems to go away.
         *
         * @return wallet balance after this transaction.
         */
        TxoValue getBalance();

        /**
         * TODO: investigate why this is always zero on testnet..
         * and sometimes on mainnet
         */
        long getConfirmations();

        boolean isIncoming();

        /**
         * Returns the timestamp when this transaction has been first seen on the network.
         *
         * <p>TODO: investigate why this returns null sometimes..
         * (same with {@link #getTxPosInBlock()}
         *
         * <p>Electrum seems to return transactions even when it does not
         * have the timestamp available - it will just return null.
         * So the timestamp may be missing if the daemon is not fully up-to-date:
         * Once electrum ran for a certain amount of time, timestamp
         * seem to be available.
         *
         * @return a timestamp when the transaction has been broadcasted
         */
        Optional<Instant> getTimestamp();

        Optional<Long> getHeight();

        /**
         * TODO: investigate why this is always null on testnet..
         * and sometimes on mainnet
         */
        Optional<Integer> getTxPosInBlock();

        Optional<String> getLabel();

        List<HistoryTxInput> getInputs();

        List<HistoryTxOutput> getOutputs();
    }

    interface HistoryTxInput {
        String getTxHash();

        long getOutputIndex();
    }

    interface HistoryTxOutput {
        TxoValue getValue();

        /**
         * Some electrum rpc responses can contain an address of an output.
         */
        Optional<String> getAddress();
    }
}
