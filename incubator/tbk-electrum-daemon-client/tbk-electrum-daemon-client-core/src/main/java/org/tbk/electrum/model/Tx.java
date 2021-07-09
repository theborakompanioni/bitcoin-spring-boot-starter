package org.tbk.electrum.model;

import java.util.List;
import java.util.Optional;

public interface Tx {
    long getLocktime();

    List<TxInput> getInputs();

    List<TxOutput> getOutputs();

    interface TxInput {
        String getTxHash();

        int getOutputIndex();

        /**
         * Some electrum rpc responses can contain an address of an input.
         */
        Optional<String> getAddress();

        long getSequenceNumber();

        Optional<String> getUnlockingScript();

        Optional<String> getWitness();

        Optional<TxoValue> getValue();
    }

    interface TxOutput {
        TxoValue getValue();

        String getLockingScript();

        /**
         * Some electrum rpc responses can contain an address of an output.
         */
        Optional<String> getAddress();
    }
}
