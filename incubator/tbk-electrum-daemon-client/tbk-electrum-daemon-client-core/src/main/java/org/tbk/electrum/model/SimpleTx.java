package org.tbk.electrum.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@Value
@Builder
public class SimpleTx implements Tx {

    long locktime;

    @Singular("addInput")
    List<TxInput> inputs;

    @Singular("addOutput")
    List<TxOutput> outputs;

    @Value
    @Builder
    public static class SimpleTxInput implements TxInput {
        @NonNull
        String txHash;

        int outputIndex;

        long sequenceNumber;

        @Nullable
        String address;

        @Nullable
        String unlockingScript;

        @Nullable
        String witness;

        @Nullable
        TxoValue value;

        @Override
        public Optional<TxoValue> getValue() {
            return Optional.ofNullable(value);
        }

        @Override
        public Optional<String> getAddress() {
            return Optional.ofNullable(address);
        }

        @Override
        public Optional<String> getUnlockingScript() {
            return Optional.ofNullable(unlockingScript);
        }

        @Override
        public Optional<String> getWitness() {
            return Optional.ofNullable(witness);
        }
    }

    @Value
    @Builder
    public static class SimpleTxOutput implements TxOutput {
        @NonNull
        TxoValue value;

        @NonNull
        String lockingScript;

        @Nullable
        String address;

        @Override
        public Optional<String> getAddress() {
            return Optional.ofNullable(address);
        }
    }
}
