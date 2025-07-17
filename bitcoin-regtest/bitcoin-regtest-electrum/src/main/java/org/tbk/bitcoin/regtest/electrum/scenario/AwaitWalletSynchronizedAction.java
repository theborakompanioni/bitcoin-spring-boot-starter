package org.tbk.bitcoin.regtest.electrum.scenario;

import com.google.common.base.Stopwatch;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.tbk.electrum.common.WalletParams;
import org.tbk.bitcoin.regtest.scenario.RegtestAction;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.rpc.command.IsSynchronizedParams;
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
    private final WalletParams params;
    private final Duration timeout;
    private final Duration checkInterval;

    public AwaitWalletSynchronizedAction(ElectrumClient client, WalletParams params) {
        this(client, params, defaultTimeout);
    }

    public AwaitWalletSynchronizedAction(ElectrumClient client, WalletParams params, Duration timeout) {
        this(client, params, timeout, defaultCheckInterval);
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "false positive")
    public AwaitWalletSynchronizedAction(ElectrumClient client, WalletParams params, Duration timeout, Duration checkInterval) {
        this.client = requireNonNull(client);
        this.params = requireNonNull(params);
        this.timeout = requireNonNull(timeout);
        this.checkInterval = requireNonNull(checkInterval);

        checkArgument(!checkInterval.isNegative(), "'checkInterval' must be positive");

        // user made a mistake when 'timeout' is smaller than or equal to 'checkInterval'
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
                    .map(it -> this.client.isWalletSynchronized(IsSynchronizedParams.builder()
                            .walletPath(params.getWalletPath())
                            .build()))
                    .filter(it -> it)
                    .blockFirst(timeout);

            requireNonNull(walletSynchronized, "electrum could not synchronize wallet in time");

            log.debug("Wallet is synchronized after {}.. ", sw.stop());

            return walletSynchronized;
        });
    }
}
