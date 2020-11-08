package org.tbk.bitcoin.txstats.example.score.label.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bitcoinj.core.*;
import org.tbk.bitcoin.txstats.example.cache.CacheFacade;
import org.tbk.bitcoin.txstats.example.score.label.ScoreLabel;
import org.tbk.bitcoin.txstats.example.score.label.ScoreLabelPredicate;

import java.util.Optional;

@AllArgsConstructor
public class RoundFeeLabelPredicate implements ScoreLabelPredicate {

    private static final ScoreLabel label = ScoreLabel.SimpleScoreLabel.builder()
            .name("round_fee")
            .description("The transaction has a round fee amount, the sender is probably using some specific software")
            .build();

    @NonNull
    private final CacheFacade caches;

    @Override
    public ScoreLabel getLabel() {
        return label;
    }

    @Override
    public boolean test(Transaction tx) {
        if (tx.isCoinBase()) {
            // coinbase has no fees
            return false;
        }

        Coin inputSum = tx.getInputs().stream()
                .filter(val -> !val.isCoinBase())
                .map(TransactionInput::getOutpoint)
                .map(outpoint -> {
                    Transaction txFromInput = caches.tx().getUnchecked(outpoint.getHash());
                    return txFromInput.getOutput(outpoint.getIndex());
                })
                .map(TransactionOutput::getValue)
                .reduce(Coin.ZERO, Coin::add);

        Coin fee = Optional.of(inputSum.minus(tx.getOutputSum()))
                .filter(Coin::isPositive)
                .orElse(Coin.ZERO);

        return fee.getValue() % 1_000L == 0L;
    }
}
