package org.tbk.bitcoin.txstats.example.score;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.bitcoinj.core.Transaction;
import org.tbk.bitcoin.txstats.example.score.label.ScoreLabel;
import org.tbk.bitcoin.txstats.example.score.label.ScoreLabelProvider;
import reactor.core.publisher.Flux;

import java.time.Instant;
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

        Instant now = Instant.now();

        TxType txType = identifyType(tx);

        Set<ScoreLabel> labels = labelProviders.stream()
                .map(val -> val.findLabels(tx))
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableSet());

        // currently just a made up score (favoring higher numbers).
        var randomScore = RandomUtils.nextInt(RandomUtils.nextInt(1, 50), 100);

        return Flux.just(1)
                .map(val -> {
                    return ScoredTransaction.builder()
                            .createdAt(now)
                            .tx(tx)
                            .type(txType)
                            .score(randomScore)
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
