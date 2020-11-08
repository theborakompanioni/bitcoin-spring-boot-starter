package org.tbk.bitcoin.txstats.example.score.label;

import org.bitcoinj.core.Transaction;

import java.util.function.Predicate;

public interface ScoreLabelPredicate extends Predicate<Transaction> {
    ScoreLabel getLabel();
}
