package org.tbk.bitcoin.txstats.example.score.label;

import org.bitcoinj.core.Transaction;

import java.util.Set;

public interface ScoreLabelProvider {
    Set<ScoreLabel> findLabels(Transaction tx);
}
