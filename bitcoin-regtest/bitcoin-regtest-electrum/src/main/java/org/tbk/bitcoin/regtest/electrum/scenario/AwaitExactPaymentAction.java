package org.tbk.bitcoin.regtest.electrum.scenario;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.reactivestreams.Subscriber;
import org.tbk.bitcoin.regtest.scenario.RegtestAction;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.TxoValue;
import org.tbk.electrum.model.Utxo;
import org.tbk.electrum.model.Utxos;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
public final class AwaitExactPaymentAction implements RegtestAction<Utxo> {
    private static final Duration defaultCheckInterval = Duration.ofMillis(100);
    private static final Duration defaultTimeout = Duration.ofSeconds(30);

    private final ElectrumClient client;
    private final Address address;
    private final Coin expectedAmount;
    private final Duration checkInterval;
    private final Duration timeout;

    public AwaitExactPaymentAction(ElectrumClient client,
                                   Coin expectedAmount,
                                   Address address) {
        this(client, expectedAmount, address, defaultCheckInterval, defaultTimeout);
    }

    public AwaitExactPaymentAction(ElectrumClient client,
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
    public void subscribe(Subscriber<? super Utxo> s) {
        create().subscribe(s);
    }

    private Flux<Utxo> create() {
        return Flux.defer(() -> {
            Stopwatch sw = Stopwatch.createStarted();

            log.debug("Poll electrum every {} till address {} controls a utxo of {} for {}",
                    this.checkInterval, this.address, this.expectedAmount.toFriendlyString(), this.timeout);

            Utxo foundUtxo = Flux.interval(this.checkInterval)
                    .doOnNext(it -> log.trace("Waiting  till address {} controls a utxo of {}.. ({} attempt)",
                            this.address, this.expectedAmount.toFriendlyString(), it))
                    .flatMapIterable(it -> {
                        Utxos addressUnspent = this.client.getAddressUnspent(this.address.toString());

                        log.trace("UTXOs: {} total", friendlyBtcString(addressUnspent.getValue()));
                        addressUnspent.getUtxos().forEach(utxo -> {
                            log.trace("       {} ({} in {})", friendlyBtcString(utxo.getValue()), utxo.getTxPos(), utxo.getTxHash());
                        });

                        return addressUnspent.getUtxos();
                    })
                    .filter(it -> {
                        Coin utxoValue = Coin.valueOf(it.getValue().getValue());
                        return utxoValue.compareTo(this.expectedAmount) == 0;
                    })
                    .blockFirst(this.timeout);

            requireNonNull(foundUtxo, "electrum could not find utxo on address in time");

            log.debug("Found utxo {} on address {} after {}.. ", foundUtxo, this.address, sw.stop());

            return Flux.just(foundUtxo);
        });
    }

    private static String friendlyBtcString(TxoValue txoValue) {
        return Coin.valueOf(txoValue.getValue()).toFriendlyString();
    }
}
