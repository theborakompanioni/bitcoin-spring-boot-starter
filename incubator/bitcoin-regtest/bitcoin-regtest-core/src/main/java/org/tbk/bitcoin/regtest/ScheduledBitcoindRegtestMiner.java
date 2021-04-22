package org.tbk.bitcoin.regtest;

import com.google.common.util.concurrent.AbstractScheduledService;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Sha256Hash;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

@Slf4j
public class ScheduledBitcoindRegtestMiner extends AbstractScheduledService implements BitcoindRegtestMiner {
    private static final Duration DEFAULT_DELAY = Duration.ofSeconds(60);
    private static final Scheduler DEFAULT_SCHEDULER = Scheduler.newFixedDelaySchedule(0, DEFAULT_DELAY.toMillis(), TimeUnit.MILLISECONDS);

    private final BitcoindRegtestMiner delegate;
    private final Scheduler scheduler;

    public ScheduledBitcoindRegtestMiner(BitcoindRegtestMiner delegate) {
        this(delegate, DEFAULT_SCHEDULER);
    }

    public ScheduledBitcoindRegtestMiner(BitcoindRegtestMiner delegate, Scheduler scheduler) {
        this.delegate = requireNonNull(delegate);
        this.scheduler = requireNonNull(scheduler);
    }

    @Override
    public List<Sha256Hash> mineBlocks(int count) {
        return delegate.mineBlocks(count);
    }

    @Override
    protected void runOneIteration() {
        this.mineBlocks(1);
    }

    @Override
    protected Scheduler scheduler() {
        return this.scheduler;
    }

}

