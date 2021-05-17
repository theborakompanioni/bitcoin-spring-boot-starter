package org.tbk.bitcoin.regtest.electrum.scenario;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Sha256Hash;
import org.reactivestreams.Subscriber;
import org.tbk.bitcoin.regtest.scenario.RegtestAction;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.History;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
public final class AwaitTransactionAction implements RegtestAction<History.Transaction> {

    private final ElectrumClient client;
    private final Sha256Hash txHash;
    private final int confirmations;

    public AwaitTransactionAction(ElectrumClient client, Sha256Hash txHash) {
        this(client, txHash, 0);
    }

    public AwaitTransactionAction(ElectrumClient client, Sha256Hash txHash, int confirmations) {
        checkArgument(confirmations >= 0, "'confirmations' must be greater than or equal to zero");

        this.client = requireNonNull(client);
        this.txHash = requireNonNull(txHash);
        this.confirmations = requireNonNull(confirmations);
    }

    @Override
    public void subscribe(Subscriber<? super History.Transaction> s) {
        create().subscribe(s);
    }

    private Flux<History.Transaction> create() {
        return Flux.defer(() -> {
            Stopwatch sw = Stopwatch.createStarted();

            /*
             * immediately after the block is mined, the electrum client sometimes
             * reports the balance as zero for a short amount of time..
             * solution: poll every 100ms for 30s as a short workaround
             */
            Duration checkInterval = Duration.ofMillis(100);
            Duration timeout = Duration.ofSeconds(30);

            log.debug("Poll electrum every {} till tx {} is processed for {}", checkInterval, txHash, timeout);

            History.Transaction broadcastedTx = Flux.interval(checkInterval)
                    .doOnNext(it -> log.trace("Waiting for tx {} to be processed by electrum.. ({} attempt)", txHash, it))
                    .flatMapIterable(it -> this.client.getHistory().getTransactions())
                    .filter(it -> txHash.toString().equalsIgnoreCase(it.getTxHash()))
                    .filter(it -> confirmations <= it.getConfirmations())
                    .blockFirst(timeout);

            requireNonNull(broadcastedTx, "electrum could not processes transaction in time");

            log.debug("Tx {} with confirmations {} has been processed by electrum after {}.. ",
                    broadcastedTx.getTxHash(), broadcastedTx.getConfirmations(), sw.stop());

            return Flux.just(broadcastedTx);
        });
    }
}
