package org.tbk.bitcoin.regtest.electrum.scenario;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.reactivestreams.Subscriber;
import org.tbk.bitcoin.regtest.scenario.RegtestAction;
import org.tbk.electrum.bitcoinj.BitcoinjElectrumClient;
import org.tbk.electrum.bitcoinj.model.BitcoinjUtxo;
import org.tbk.electrum.bitcoinj.model.BitcoinjUtxos;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
public final class AwaitExactPaymentAction implements RegtestAction<BitcoinjUtxo> {
    private static final Duration defaultTimeout = Duration.ofSeconds(30);
    private static final Duration defaultCheckInterval = Duration.ofMillis(100);

    private final BitcoinjElectrumClient client;
    private final Address address;
    private final Coin expectedAmount;
    private final Duration timeout;
    private final Duration checkInterval;

    public AwaitExactPaymentAction(BitcoinjElectrumClient client,
                                   Coin expectedAmount,
                                   Address address) {
        this(client, expectedAmount, address, defaultTimeout, defaultCheckInterval);
    }

    public AwaitExactPaymentAction(BitcoinjElectrumClient client,
                                   Coin expectedAmount,
                                   Address address,
                                   Duration timeout,
                                   Duration checkInterval) {
        this.client = requireNonNull(client);
        this.address = requireNonNull(address);
        this.expectedAmount = requireNonNull(expectedAmount);
        this.timeout = requireNonNull(timeout);
        this.checkInterval = requireNonNull(checkInterval);

        checkArgument(expectedAmount.isPositive(), "'expectedAmount' must be positive");
        checkArgument(!checkInterval.isNegative(), "'checkInterval' must be positive");

        // users may made a mistake when 'timeout' is smaller than or equal to 'checkInterval'
        checkArgument(timeout.compareTo(checkInterval) > 0, "'timeout' must be greater than 'checkInterval");
    }

    @Override
    public void subscribe(Subscriber<? super BitcoinjUtxo> s) {
        create().subscribe(s);
    }

    private Mono<BitcoinjUtxo> create() {
        return Mono.fromCallable(() -> {
            Stopwatch sw = Stopwatch.createStarted();

            log.debug("Poll electrum every {} till address {} controls a utxo of {} for {}",
                    this.checkInterval, this.address, this.expectedAmount.toFriendlyString(), this.timeout);

            BitcoinjUtxo foundUtxo = Flux.interval(this.checkInterval)
                    .doOnNext(it -> log.trace("Waiting  till address {} controls a utxo of {}.. ({} attempt)",
                            this.address, this.expectedAmount.toFriendlyString(), it))
                    .flatMapIterable(it -> {
                        BitcoinjUtxos addressUnspent = this.client.getAddressUnspent(this.address);

                        log.trace("UTXOs: {} total", addressUnspent.getValue().toFriendlyString());
                        for (BitcoinjUtxo utxo : addressUnspent.getUtxos()) {
                            log.trace("       {} ({} in {})", utxo.getValue().toFriendlyString(), utxo.getTxPos(), utxo.getTxHash());
                        }

                        return addressUnspent.getUtxos();
                    })
                    .filter(it -> {
                        Coin utxoValue = Coin.valueOf(it.getValue().getValue());
                        return utxoValue.compareTo(this.expectedAmount) == 0;
                    })
                    .blockFirst(this.timeout);

            requireNonNull(foundUtxo, "electrum could not find utxo on address in time");

            log.debug("Found utxo {} on address {} after {}.. ", foundUtxo, this.address, sw.stop());

            return foundUtxo;
        });
    }
}
