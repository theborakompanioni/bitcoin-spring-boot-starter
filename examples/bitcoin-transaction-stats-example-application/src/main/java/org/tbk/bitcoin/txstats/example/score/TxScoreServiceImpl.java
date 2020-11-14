package org.tbk.bitcoin.txstats.example.score;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Transaction;
import org.tbk.bitcoin.common.bitcoinj.util.MoreScripts;
import org.tbk.bitcoin.txstats.example.score.label.ScoreLabel;
import org.tbk.bitcoin.txstats.example.score.label.ScoreLabelProvider;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Collection;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@AllArgsConstructor
public class TxScoreServiceImpl implements TxScoreService {

    @NonNull
    private final List<ScoreLabelProvider> labelProviders;

    @NonNull
    private final List<AddressScoreProvider> addressScoreProviders;

    @Override
    public Flux<ScoredTransaction> scoreTransaction(Transaction tx) {

        Instant now = Instant.now();

        TxType txType = identifyType(tx);

        Set<ScoreLabel> labels = labelProviders.stream()
                .map(val -> val.findLabels(tx))
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableSet());


        List<Address> outputAddresses = MoreScripts.extractOutputAddress(tx);

        List<AddressScoreInput> addressScoreInputParams = outputAddresses.stream()
                .map(address -> AddressScoreInput.newBuilder()
                        .setAddress(AddressInput.newBuilder()
                                .setType(address.getOutputScriptType().name())
                                .setAddress(address.toString())
                                .build())
                        .build())
                .collect(Collectors.toList());

        List<AddressScoreAnalysis> analyses = addressScoreInputParams.stream()
                .flatMap(scoreInput -> addressScoreProviders.stream()
                        .map(provider -> provider.gradeAddress(scoreInput))
                        .flatMap(Collection::stream)
                )
                .collect(Collectors.toList());

        List<ScoreValue> scoreValues = analyses.stream()
                .map(AddressScoreAnalysis::getScore)
                .map(AddressScore::getValue)
                .collect(Collectors.toList());

        IntSummaryStatistics scoreSummaryStatistics = scoreValues.stream()
                .filter(val -> val.getConfidence().getValue() > 0)
                .mapToInt(ScoreValue::getValue)
                .summaryStatistics();

        long score = scoreSummaryStatistics.getCount() > 0L ?
                Math.round(scoreSummaryStatistics.getMin()) :
                99;

        IntSummaryStatistics confidenceSummaryStatistics = scoreValues.stream()
                .filter(val -> val.getConfidence().getValue() > 0)
                .map(ScoreValue::getConfidence)
                .mapToInt(ScoreConfidence::getValue)
                .summaryStatistics();

        long confidence = scoreSummaryStatistics.getCount() > 0L ?
                Math.round(confidenceSummaryStatistics.getAverage()) :
                0;

        return Flux.just(1)
                .map(val -> {

                    // currently just a made up score (favoring higher numbers).
                    // this is just for "demo" reasons - remove if real values are provided.
                    var randomScore = RandomUtils.nextInt(RandomUtils.nextInt(1, 50), 100);
                    long adaptedScore = Math.min(score, randomScore);

                    return ScoredTransaction.builder()
                            .createdAt(now)
                            .tx(tx)
                            .type(txType)
                            .score(adaptedScore)
                            .confidence(confidence)
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
