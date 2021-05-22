package org.tbk.bitcoin.regtest.electrum.scenario;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Coin;
import org.reactivestreams.Subscriber;
import org.tbk.bitcoin.regtest.scenario.RegtestAction;
import org.tbk.electrum.bitcoinj.BitcoinjElectrumClient;
import org.tbk.electrum.bitcoinj.model.BitcoinjBalance;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
public final class AwaitSpendableBalanceAction implements RegtestAction<Coin> {
    private static final Duration defaultCheckInterval = Duration.ofMillis(100);
    private static final Duration defaultTimeout = Duration.ofSeconds(30);

    private final BitcoinjElectrumClient client;
    private final Coin expectedAmount;
    private final Duration checkInterval;
    private final Duration timeout;

    public AwaitSpendableBalanceAction(BitcoinjElectrumClient client,
                                       Coin expectedAmount) {
        this(client, expectedAmount, defaultCheckInterval, defaultTimeout);
    }

    public AwaitSpendableBalanceAction(BitcoinjElectrumClient client,
                                       Coin expectedAmount,
                                       Duration checkInterval,
                                       Duration timeout) {
        this.client = requireNonNull(client);
        this.expectedAmount = requireNonNull(expectedAmount);
        this.checkInterval = requireNonNull(checkInterval);
        this.timeout = requireNonNull(timeout);

        checkArgument(expectedAmount.isPositive(), "'expectedAmount' must be positive");
        checkArgument(!checkInterval.isNegative(), "'checkInterval' must be positive");

        // users may made a mistake when 'timeout' is smaller than or equal to 'checkInterval'
        checkArgument(timeout.compareTo(checkInterval) > 0, "'timeout' must be greater than 'checkInterval");
    }

    @Override
    public void subscribe(Subscriber<? super Coin> s) {
        create().subscribe(s);
    }

    private Flux<Coin> create() {
        return Flux.defer(() -> {
            Stopwatch sw = Stopwatch.createStarted();

            log.debug("Poll electrum every {} till balance reaches {} for {}",
                    this.checkInterval, this.expectedAmount.toFriendlyString(), this.timeout);

            Coin coin = Flux.interval(this.checkInterval)
                    .doOnNext(it -> log.trace("Waiting balance of {} by electrum.. ({} attempt)",
                            this.expectedAmount.toFriendlyString(), it))
                    .map(it -> this.client.getBalance())
                    .doOnNext(balance -> {
                        log.trace("Balance: {} total", balance.getTotal().toFriendlyString());
                        log.trace("         {} confirmed", balance.getConfirmed().toFriendlyString());
                        log.trace("         {} unconfirmed", balance.getUnconfirmed().toFriendlyString());
                        log.trace("         {} spendable", balance.getSpendable().toFriendlyString());
                        log.trace("         {} unmatured", balance.getUnmatured().toFriendlyString());
                    })
                    .map(BitcoinjBalance::getSpendable)
                    .filter(it -> !it.isLessThan(this.expectedAmount))
                    .blockFirst(this.timeout);

            requireNonNull(coin, "electrum could not find balance in time");

            log.debug("Found balance {} after {}.. ", coin.toFriendlyString(), sw.stop());

            return Flux.just(coin);
        });
    }
}
