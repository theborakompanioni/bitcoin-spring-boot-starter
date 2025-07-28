package org.tbk.bitcoin.regtest.mining;

import com.google.common.util.concurrent.AbstractScheduledService;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

import static java.util.Objects.requireNonNull;

@Slf4j
public final class ScheduledRegtestMining extends AbstractScheduledService {
    private static final Duration DEFAULT_DELAY = Duration.ofSeconds(60);
    private static final Scheduler DEFAULT_SCHEDULER = Scheduler.newFixedDelaySchedule(Duration.ZERO, DEFAULT_DELAY);

    private final RegtestMiner delegate;
    private final Scheduler scheduler;

    public ScheduledRegtestMining(RegtestMiner delegate) {
        this(delegate, DEFAULT_SCHEDULER);
    }

    public ScheduledRegtestMining(RegtestMiner delegate, Scheduler scheduler) {
        this.delegate = requireNonNull(delegate);
        this.scheduler = requireNonNull(scheduler);
    }

    @Override
    protected void runOneIteration() {
        delegate.mineBlocks(1);
    }

    @Override
    protected Scheduler scheduler() {
        return this.scheduler;
    }

}

