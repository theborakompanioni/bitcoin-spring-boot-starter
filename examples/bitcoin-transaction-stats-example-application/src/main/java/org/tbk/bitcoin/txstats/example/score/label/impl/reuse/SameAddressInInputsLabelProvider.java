package org.tbk.bitcoin.txstats.example.score.label.impl.reuse;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.tbk.bitcoin.txstats.example.cache.AppCacheFacade;
import org.tbk.bitcoin.txstats.example.score.label.ScoreLabel;
import org.tbk.bitcoin.txstats.example.score.label.ScoreLabelPredicate;
import org.tbk.bitcoin.txstats.example.util.MoreScripts;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class SameAddressInInputsLabelProvider implements ScoreLabelPredicate {

    private static final ScoreLabel label = ScoreLabel.SimpleScoreLabel.builder()
            .name("simple_reuse_same_address_in_inputs")
            .description("There's multiple occurrences of the same address in inputs")
            .build();

    @NonNull
    private final NetworkParameters networkParameters;

    @NonNull
    private final AppCacheFacade caches;

    @Override
    public ScoreLabel getLabel() {
        return label;
    }

    @Override
    public boolean test(Transaction tx) {
        if (tx.isCoinBase()) {
            return false;
        }

        List<String> inputAddresses = tx.getInputs().stream()
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
                .collect(Collectors.toUnmodifiableList());

        Set<String> uniqueInputAddresses = Sets.newHashSet(inputAddresses);

        return inputAddresses.size() > uniqueInputAddresses.size();
    }
}
