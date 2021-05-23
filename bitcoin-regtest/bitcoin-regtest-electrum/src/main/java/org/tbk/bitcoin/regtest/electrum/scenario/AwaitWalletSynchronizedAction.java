package org.tbk.bitcoin.regtest.electrum.scenario;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.tbk.bitcoin.regtest.scenario.RegtestAction;
import org.tbk.electrum.ElectrumClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
public final class AwaitWalletSynchronizedAction implements RegtestAction<Boolean> {
    private static final Duration defaultTimeout = Duration.ofSeconds(30);
    private static final Duration defaultCheckInterval = Duration.ofMillis(100);

    private final ElectrumClient client;
    private final Duration timeout;
    private final Duration checkInterval;

    public AwaitWalletSynchronizedAction(ElectrumClient client) {
        this(client, defaultTimeout);
    }

    public AwaitWalletSynchronizedAction(ElectrumClient client, Duration timeout) {
        this(client, timeout, defaultCheckInterval);
    }

    public AwaitWalletSynchronizedAction(ElectrumClient client, Duration timeout, Duration checkInterval) {

        this.client = requireNonNull(client);
        this.timeout = requireNonNull(timeout);
        this.checkInterval = requireNonNull(checkInterval);

        checkArgument(!checkInterval.isNegative(), "'checkInterval' must be positive");

        // users may made a mistake when 'timeout' is smaller than or equal to 'checkInterval'
        checkArgument(timeout.compareTo(checkInterval) > 0, "'timeout' must be greater than 'checkInterval");
    }

    @Override
    public void subscribe(Subscriber<? super Boolean> s) {
        create().subscribe(s);
    }

    private Mono<Boolean> create() {
        return Mono.fromCallable(() -> {
            Stopwatch sw = Stopwatch.createStarted();

            log.debug("Poll electrum every {} till wallet is synchronized for {}", this.checkInterval, this.timeout);

            Boolean walletSynchronized = Flux.interval(checkInterval)
                    .doOnNext(it -> log.trace("Waiting for wallet to be synchronized.. ({} attempt)", it))
                    .map(it -> this.client.isWalletSynchronized())
                    .filter(it -> it)
                    .blockFirst(timeout);

            requireNonNull(walletSynchronized, "electrum could not synchronize wallet in time");

            log.debug("Wallet is synchronized after {}.. ", sw.stop());

            return walletSynchronized;
        });
    }
}
