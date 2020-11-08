package org.tbk.bitcoin.txstats.example.score.api;

import com.google.common.collect.ImmutableMap;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbk.bitcoin.txstats.example.cache.CacheFacade;
import org.tbk.bitcoin.txstats.example.score.TxScoreService;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static java.util.Objects.requireNonNull;


@RestController
@RequestMapping(value = "/api/v1/tx/score")
public class TxScoreCtrl {

    private final CacheFacade caches;
    private final TxScoreService txScoreService;

    public TxScoreCtrl(CacheFacade caches, TxScoreService txScoreService) {
        this.caches = requireNonNull(caches);
        this.txScoreService = requireNonNull(txScoreService);
    }

    @GetMapping("/{txId}")
    public ResponseEntity<? extends Map<String, Object>> score(@PathVariable("txId") String txIdParam) {

        Optional<Sha256Hash> txIdOrEmpty = toTxId(txIdParam);
        if (txIdOrEmpty.isEmpty()) {
            return ResponseEntity.badRequest().body(ImmutableMap.<String, Object>builder()
                    .put("message", "Could not decode tx_id")
                    .build());
        }

        Sha256Hash txId = txIdOrEmpty.get();

        Optional<Transaction> txOrEmpty = fetchTransaction(txId);
        if (txOrEmpty.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Transaction tx = txOrEmpty.get();

        TxScoreService.ScoredTransaction scoredTransaction = txScoreService.scoreTransaction(tx)
                .blockFirst();

        ImmutableMap<String, Object> result = ImmutableMap.<String, Object>builder()
                .put("tx_id", tx.getTxId().toString())
                .put("score", scoredTransaction.getScore())
                .put("type", scoredTransaction.getType())
                .put("labels", scoredTransaction.getLabels())
                .build();

        return ResponseEntity.ok(result);
    }

    private Optional<Transaction> fetchTransaction(Sha256Hash txId) {
        try {
            return Optional.ofNullable(caches.tx().get(txId));
        } catch (ExecutionException e) {
            return Optional.empty();
        }
    }

    private Optional<Sha256Hash> toTxId(String txId) {
        try {
            return Optional.of(Sha256Hash.wrap(txId));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
