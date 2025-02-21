package org.tbk.bitcoin.regtest.electrum.scenario;

import com.google.common.base.Stopwatch;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Sha256Hash;
import org.reactivestreams.Subscriber;
import org.tbk.bitcoin.regtest.scenario.RegtestAction;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.History;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
public final class AwaitTransactionAction implements RegtestAction<History.Transaction> {
    private static final Duration defaultTimeout = Duration.ofSeconds(30);
    private static final Duration defaultCheckInterval = Duration.ofMillis(100);
    private static final int DEFAULT_CONFIRMATIONS = 0;

    private final ElectrumClient client;
    private final Sha256Hash txHash;
    private final int confirmations;
    private final Duration timeout;
    private final Duration checkInterval;

    public AwaitTransactionAction(ElectrumClient client, Sha256Hash txHash) {
        this(client, txHash, DEFAULT_CONFIRMATIONS);
    }

    public AwaitTransactionAction(ElectrumClient client, Sha256Hash txHash, int confirmations) {
        this(client, txHash, confirmations, defaultTimeout);
    }

    public AwaitTransactionAction(ElectrumClient client, Sha256Hash txHash, Duration timeout) {
        this(client, txHash, DEFAULT_CONFIRMATIONS, timeout, defaultCheckInterval);
    }

    public AwaitTransactionAction(ElectrumClient client, Sha256Hash txHash, int confirmations, Duration timeout) {
        this(client, txHash, confirmations, timeout, defaultCheckInterval);
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "false positive")
    public AwaitTransactionAction(ElectrumClient client, Sha256Hash txHash, int confirmations, Duration timeout, Duration checkInterval) {
        checkArgument(confirmations >= 0, "'confirmations' must be greater than or equal to zero");

        this.client = requireNonNull(client);
        this.txHash = requireNonNull(txHash);
        this.confirmations = confirmations;
        this.timeout = requireNonNull(timeout);
        this.checkInterval = requireNonNull(checkInterval);

        checkArgument(!checkInterval.isNegative(), "'checkInterval' must be positive");

        // user made a mistake when 'timeout' is smaller than or equal to 'checkInterval'
        checkArgument(timeout.compareTo(checkInterval) > 0, "'timeout' must be greater than 'checkInterval");
    }

    @Override
    public void subscribe(Subscriber<? super History.Transaction> s) {
        create().subscribe(s);
    }

    private Mono<History.Transaction> create() {
        return Mono.fromCallable(() -> {
            Stopwatch sw = Stopwatch.createStarted();

            /*
             * immediately after the block is mined, the electrum client sometimes
             * reports the balance as zero for a short amount of time..
             * solution: poll every `checkInterval` for ``timeout` amount of time as a short workaround
             */
            log.debug("Poll electrum every {} till tx {} is processed for {}", this.checkInterval, this.txHash, this.timeout);

            History.Transaction broadcastedTx = Flux.interval(this.checkInterval)
                    .doOnNext(it -> log.trace("Waiting for tx {} to be processed by electrum.. ({} attempt)", this.txHash, it))
                    .flatMapIterable(it -> this.client.getHistory().getTransactions())
                    .filter(it -> this.txHash.toString().equalsIgnoreCase(it.getTxHash()))
                    .filter(it -> it.getConfirmations() >= this.confirmations)
                    .blockFirst(this.timeout);

            requireNonNull(broadcastedTx, "electrum could not processes transaction in time");

            log.debug("Tx {} with {} confirmations has been processed by electrum after {}.. ",
                    broadcastedTx.getTxHash(), broadcastedTx.getConfirmations(), sw.stop());

            return broadcastedTx;
        });
    }
}
