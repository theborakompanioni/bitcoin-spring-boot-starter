package org.tbk.spring.testcontainer.bitcoind.regtest;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@Slf4j
public class ScheduledBitcoindRegtestMiner extends AbstractScheduledService {
    private static final Duration DEFAULT_DELAY = Duration.ofSeconds(60);
    private static final Scheduler DEFAULT_SCHEDULER = Scheduler.newFixedDelaySchedule(0, DEFAULT_DELAY.toMillis(), TimeUnit.MILLISECONDS);

    private final Scheduler scheduler;
    private final BitcoinClient client;
    private final CoinbaseRewardAddressSupplier coinbaseRewardAddressSupplier;

    public ScheduledBitcoindRegtestMiner(BitcoinClient client) {
        this(client, DEFAULT_SCHEDULER);
    }

    public ScheduledBitcoindRegtestMiner(BitcoinClient client, Scheduler scheduler) {
        this(client, scheduler, new BitcoinClientCoinbaseRewardAddressSupplier(client));
    }

    public ScheduledBitcoindRegtestMiner(BitcoinClient client, Scheduler scheduler, CoinbaseRewardAddressSupplier coinbaseRewardAddressSupplier) {
        this.client = requireNonNull(client);
        this.scheduler = requireNonNull(scheduler);
        this.coinbaseRewardAddressSupplier = requireNonNull(coinbaseRewardAddressSupplier);
    }

    @Override
    protected void runOneIteration() throws Exception {
        Address coinbaseRewardAddress = this.coinbaseRewardAddressSupplier.get();

        log.debug("Trying to mine one block with coinbase reward for address {}", coinbaseRewardAddress);

        List<Sha256Hash> sha256Hashes = this.client.generateToAddress(1, coinbaseRewardAddress);

        log.debug("Mined {} blocks with coinbase reward for address {}", sha256Hashes.size(), coinbaseRewardAddress);
    }

    @Override
    protected Scheduler scheduler() {
        return this.scheduler;
    }
}

