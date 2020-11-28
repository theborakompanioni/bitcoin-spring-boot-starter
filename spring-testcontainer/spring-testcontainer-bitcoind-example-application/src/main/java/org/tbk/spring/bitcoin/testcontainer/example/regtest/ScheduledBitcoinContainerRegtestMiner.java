package org.tbk.spring.bitcoin.testcontainer.example.regtest;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

@Slf4j
public class ScheduledBitcoinContainerRegtestMiner extends AbstractScheduledService {
    private static final Duration DEFAULT_DELAY = Duration.ofSeconds(60);
    private static final Scheduler DEFAULT_SCHEDULER = Scheduler.newFixedDelaySchedule(0, DEFAULT_DELAY.toMillis(), TimeUnit.MILLISECONDS);

    private final Scheduler scheduler;
    private final BitcoinClient client;
    private final Address addressOrNull;

    public ScheduledBitcoinContainerRegtestMiner(BitcoinClient client) {
        this(client, DEFAULT_SCHEDULER);
    }

    public ScheduledBitcoinContainerRegtestMiner(BitcoinClient client, Scheduler scheduler) {
        this(client, scheduler, null);
    }

    public ScheduledBitcoinContainerRegtestMiner(BitcoinClient client, Scheduler scheduler, Address address) {
        this.client = requireNonNull(client);
        this.scheduler = requireNonNull(scheduler);
        this.addressOrNull = address;
    }

    @Override
    protected void runOneIteration() throws Exception {
        Address address = Optional.ofNullable(addressOrNull)
                .orElseGet(() -> {
                    try {
                        return client.getNewAddress();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        this.client.generateToAddress(1, address);
    }

    @Override
    protected Scheduler scheduler() {
        return scheduler;
    }
}

