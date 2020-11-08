package org.tbk.bitcoin.txstats.example.score;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Transaction;
import org.tbk.bitcoin.txstats.example.score.label.ScoreLabel;
import org.tbk.bitcoin.txstats.example.score.label.ScoreLabelProvider;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class TxScoreServiceImpl implements TxScoreService {

    @NonNull
    private final List<ScoreLabelProvider> labelProviders;

    @Override
    public Flux<ScoredTransaction> scoreTransaction(Transaction tx) {

        TxType txType = identifyType(tx);

        Set<ScoreLabel> labels = labelProviders.stream()
                .map(val -> val.findLabels(tx))
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableSet());

        return Flux.just(1)
                .map(val -> {
                    return ScoredTransaction.builder()
                            .tx(tx)
                            .type(txType)
                            .score(0)
                            .labels(labels)
                            .build();
                });
    }


    private TxType identifyType(Transaction tx) {
        if (tx.isCoinBase()) {
            return TxType.CB;
        }
        int outputSize = tx.getOutputs().size();
        if (outputSize == 0) {
            throw new IllegalArgumentException("Transaction must have at least one output");
        }
        if (outputSize == 1) {
            return TxType.N1;
        }
        if (outputSize == 2) {
            return TxType.N2;
        }
        return TxType.NN;
    }
}
