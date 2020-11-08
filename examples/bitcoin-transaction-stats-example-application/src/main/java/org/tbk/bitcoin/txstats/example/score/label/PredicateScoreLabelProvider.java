package org.tbk.bitcoin.txstats.example.score.label;

import org.bitcoinj.core.Transaction;

import java.util.Collections;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class PredicateScoreLabelProvider implements ScoreLabelProvider {

    private final ScoreLabelPredicate predicte;

    public PredicateScoreLabelProvider(ScoreLabelPredicate predicate) {
        this.predicte = requireNonNull(predicate);
    }

    public Set<ScoreLabel> findLabels(Transaction tx) {
        if (predicte.test(tx)) {
            return Collections.singleton(predicte.getLabel());
        }

        return Collections.emptySet();
    }
}
