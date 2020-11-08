package org.tbk.bitcoin.txstats.example.score.label.impl.reuse;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bitcoinj.core.*;
import org.tbk.bitcoin.txstats.example.cache.CacheFacade;
import org.tbk.bitcoin.txstats.example.score.label.ScoreLabel;
import org.tbk.bitcoin.txstats.example.score.label.ScoreLabelPredicate;
import org.tbk.bitcoin.txstats.example.util.MoreScripts;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class AddressReuseLabelPredicate implements ScoreLabelPredicate {

    private static final ScoreLabel label = ScoreLabel.SimpleScoreLabel.of("address_reuse");

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

        Set<String> outputAddresses = tx.getOutputs().stream()
                .map(TransactionOutput::getScriptPubKey)
                .map(val -> MoreScripts.extractAddress(networkParameters, val))
                .flatMap(Optional::stream)
                .map(Object::toString)
                .collect(Collectors.toUnmodifiableSet());

        if (outputAddresses.isEmpty()) {
            // outputs can be empty e.g. when OP_RETURN is used
            return false;
        }

        Set<String> inputAddresses = tx.getInputs().stream()
                .filter(val -> !val.isCoinBase())
                .map(TransactionInput::getOutpoint)
                .map(val -> {
                    Transaction txFromInput = caches.tx().getUnchecked(val.getHash());
                    return txFromInput.getOutput(val.getIndex());
                })
                .map(TransactionOutput::getScriptPubKey)
                .map(val -> MoreScripts.extractAddress(networkParameters, val))
                .flatMap(Optional::stream)
                .map(Object::toString)
                .collect(Collectors.toUnmodifiableSet());

        Sets.SetView<String> reusedAddresses = Sets.intersection(outputAddresses, inputAddresses);

        return !reusedAddresses.isEmpty();
    }
}
