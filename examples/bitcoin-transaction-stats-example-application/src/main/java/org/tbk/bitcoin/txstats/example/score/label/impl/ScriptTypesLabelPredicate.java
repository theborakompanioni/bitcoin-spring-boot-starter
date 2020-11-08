package org.tbk.bitcoin.txstats.example.score.label.impl;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.tbk.bitcoin.txstats.example.cache.CacheFacade;
import org.tbk.bitcoin.txstats.example.score.label.ScoreLabel;
import org.tbk.bitcoin.txstats.example.score.label.ScoreLabelPredicate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ScriptTypesLabelPredicate implements ScoreLabelPredicate {

    private static final ScoreLabel label = ScoreLabel.SimpleScoreLabel.builder()
            .name("script_types")
            .description("If all inputs has the same type, and exactly one of the outputs is not of the same type — this output can be considered as the recipient")
            .build();

    @NonNull
    private final NetworkParameters networkParameters;

    @NonNull
    private final CacheFacade caches;

    @Override
    public ScoreLabel getLabel() {
        return label;
    }

    @Override
    public boolean test(Transaction tx) {
        if (tx.isCoinBase()) {
            return false;
        }

        List<Script.ScriptType> outputScriptTypes = tx.getOutputs().stream()
                .map(TransactionOutput::getScriptPubKey)
                .map(val -> Optional.ofNullable(val.getScriptType()))
                .flatMap(Optional::stream)
                .collect(Collectors.toUnmodifiableList());

        if (outputScriptTypes.isEmpty()) {
            // outputs can be empty e.g. when OP_RETURN is used
            return false;
        }

        List<Script.ScriptType> inputScriptTypes = tx.getInputs().stream()
                .filter(val -> !val.isCoinBase())
                .map(TransactionInput::getOutpoint)
                .map(val -> {
                    Transaction txFromInput = caches.tx().getUnchecked(val.getHash());
                    return txFromInput.getOutput(val.getIndex());
                })
                .map(TransactionOutput::getScriptPubKey)
                .map(val -> Optional.ofNullable(val.getScriptType()))
                .flatMap(Optional::stream)
                .collect(Collectors.toUnmodifiableList());

        boolean allInputsHaveSameType = Sets.newHashSet(inputScriptTypes).size() == 1;
        if (!allInputsHaveSameType) {
            return false;
        }

        Script.ScriptType inputScriptType = inputScriptTypes.get(0);

        long amountOfMissmatchingScriptTypesInOutput = outputScriptTypes.stream()
                .filter(val -> !inputScriptType.equals(val))
                .count();

        return amountOfMissmatchingScriptTypesInOutput == 1L;
    }
}
