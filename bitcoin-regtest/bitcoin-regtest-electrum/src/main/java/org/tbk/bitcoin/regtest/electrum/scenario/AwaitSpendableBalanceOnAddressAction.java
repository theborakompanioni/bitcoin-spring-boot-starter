package org.tbk.bitcoin.regtest.electrum.scenario;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
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
public final class AwaitSpendableBalanceOnAddressAction implements RegtestAction<Coin> {
    private static final Duration defaultCheckInterval = Duration.ofMillis(100);
    private static final Duration defaultTimeout = Duration.ofSeconds(30);

    private final BitcoinjElectrumClient client;
    private final Address address;
    private final Coin expectedAmount;
    private final Duration checkInterval;
    private final Duration timeout;

    public AwaitSpendableBalanceOnAddressAction(BitcoinjElectrumClient client,
                                                Coin expectedAmount,
                                                Address address) {
        this(client, expectedAmount, address, defaultCheckInterval, defaultTimeout);
    }

    public AwaitSpendableBalanceOnAddressAction(BitcoinjElectrumClient client,
                                                Coin expectedAmount,
                                                Address address,
                                                Duration checkInterval,
                                                Duration timeout) {
        this.client = requireNonNull(client);
        this.address = requireNonNull(address);
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

            log.debug("Poll electrum every {} till balance of address {} reaches {} for {}",
                    this.checkInterval, this.address, this.expectedAmount.toFriendlyString(), this.timeout);

            Coin coin = Flux.interval(this.checkInterval)
                    .doOnNext(it -> log.trace("Waiting for coinbase reward to be spendable by electrum.. ({} attempt)", it))
                    .map(it -> this.client.getAddressBalance(this.address))
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

            requireNonNull(coin, "electrum could not find balance on address in time");

            log.debug("Found balance {} on address {} after {}.. ", coin.toFriendlyString(), this.address, sw.stop());

            return Flux.just(coin);
        });
    }
}
