package org.tbk.electrum.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import org.tbk.electrum.rpc.command.OnchainHistoryResponse;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.tbk.electrum.model.BtcTxoValues.fromBtcString;

@Value
@Builder
public class SimpleOnchainHistory implements OnchainHistory {
    public static SimpleOnchainHistory from(List<OnchainHistoryResponse.HistoricTransaction> val) {
        return SimpleOnchainHistory.builder()
                .transactions(val.stream()
                        .map(SimpleOnchainHistory.SimpleTransaction::from)
                        .toList())
                .build();
    }


    @Singular("addTransaction")
    List<Transaction> transactions;

    @Value
    @Builder
    public static class SimpleTransaction implements Transaction {
        public static SimpleTransaction from(OnchainHistoryResponse.HistoricTransaction val) {
            List<SimpleOnchainHistory.SimpleHistoryTxInput> inputsOrEmpty = Optional.ofNullable(val.getInputs())
                    .map(inputs -> inputs.stream()
                            .map(input -> SimpleOnchainHistory.SimpleHistoryTxInput.builder()
                                    .txHash(input.getPrevoutHash())
                                    .outputIndex(input.getPrevoutN())
                                    .build())
                            .toList())
                    .orElseGet(Collections::emptyList);

            List<SimpleOnchainHistory.SimpleHistoryTxOutput> outputsOrEmpty = Optional.ofNullable(val.getOutputs())
                    .map(outputs -> outputs.stream()
                            .map(output -> SimpleOnchainHistory.SimpleHistoryTxOutput.builder()
                                    .value(SimpleTxoValue.of(output.getValueSat()))
                                    .address(output.getAddress())
                                    .build())
                            .toList())
                    .orElseGet(Collections::emptyList);

            Instant timestampOrNull = Optional.ofNullable(val.getTimestamp())
                    .map(Instant::ofEpochSecond)
                    .orElse(null);

            return SimpleOnchainHistory.SimpleTransaction.builder()
                    .balance(fromBtcString(val.getBalance()))
                    .txHash(val.getTxId())
                    .value(fromBtcString(val.getValue()))
                    .incoming(val.isIncoming())
                    .confirmations(val.getConfirmations())
                    .timestamp(timestampOrNull)
                    .height(val.getHeight())
                    .label(val.getLabel())
                    .txPosInBlock(val.getTxPosInBlock())
                    .inputs(inputsOrEmpty)
                    .outputs(outputsOrEmpty)
                    .build();
        }

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
