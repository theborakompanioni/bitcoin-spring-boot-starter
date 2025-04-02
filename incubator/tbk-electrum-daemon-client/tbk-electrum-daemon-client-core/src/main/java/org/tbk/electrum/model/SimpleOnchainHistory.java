package org.tbk.electrum.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Value
@Builder
public class SimpleOnchainHistory implements OnchainHistory {

    @NonNull
    Summary summary;

    @Singular("addTransaction")
    List<Transaction> transactions;

    @Value
    @Builder
    public static class SimpleSummary implements Summary {
        @NonNull
        TxoValue startBalance;

        @NonNull
        TxoValue endBalance;

        @NonNull
        TxoValue incoming;

        @NonNull
        TxoValue outgoing;
    }

    @Value
    @Builder
    public static class SimpleTransaction implements Transaction {
        @NonNull
        TxoValue balance;

        @NonNull
        TxoValue value;

        @NonNull
        String txHash;

        long confirmations;

        @Nullable
        Instant timestamp;

        @Nullable
        Long height;

        boolean incoming;

        @Nullable
        String label;

        @Nullable
        Integer txPosInBlock;

        @Singular("addInput")
        List<HistoryTxInput> inputs;

        @Singular("addOutput")
        List<HistoryTxOutput> outputs;

        @Override
        public Optional<Instant> getTimestamp() {
            return Optional.ofNullable(timestamp);
        }

        @Override
        public Optional<Integer> getTxPosInBlock() {
            return Optional.ofNullable(txPosInBlock);
        }

        @Override
        public Optional<Long> getHeight() {
            return Optional.ofNullable(height);
        }

        @Override
        public Optional<String> getLabel() {
            return Optional.ofNullable(label);
        }
    }

    @Value
    @Builder
    public static class SimpleHistoryTxInput implements OnchainHistory.HistoryTxInput {
        @NonNull
        String txHash;

        long outputIndex;
    }

    @Value
    @Builder
    public static class SimpleHistoryTxOutput implements OnchainHistory.HistoryTxOutput {

        @NonNull
        TxoValue value;

        @Nullable
        String address;

        @Override
        public Optional<String> getAddress() {
            return Optional.ofNullable(address);
        }
    }
}
