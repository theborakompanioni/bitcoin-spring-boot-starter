package org.tbk.bitcoin.txstats.example.score;

import com.google.common.base.MoreObjects;
import lombok.*;
import org.bitcoinj.core.Transaction;
import org.tbk.bitcoin.txstats.example.score.label.ScoreLabel;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public interface TxScoreService {

    enum TxType {
        CB("Coinbase transaction"),
        N1("Transaction with just 1 output (either a sweep to another address by the same owner, or a transfer using a 'send everything I have' option)"),
        N2("Transaction with 2 outputs — most common in wallets — where one of the outputs is the recipient, and the other one is the change address"),
        NN("Transaction with more than 2 outputs — most common in exchanges and services that use payout batching"),
        ;

        private final String description;

        TxType(String description) {
            this.description = requireNonNull(description);
        }
    }

    @Value
    @Builder
    class ScoredTransaction {
        @Builder.Default
        Instant createdAt = Instant.now();

        @NonNull
        Transaction tx;

        long score;

        TxType type;

        // shows whether the transaction has the final score — true if all outputs are spent, false otherwise (nulldata and other non-spendable outputs are considered as spent)
        boolean finalized;

        @Singular("addCluster")
        Set<String> clusterized;

        @Singular("addLabel")
        Set<ScoreLabel> labels;

        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("txId", tx.getTxId())
                    .add("score", score)
                    .add("type", type)
                    .add("finalized", finalized)
                    .add("labels", labels)
                    .toString();
        }
    }

    Flux<ScoredTransaction> scoreTransaction(Transaction tx);

}
