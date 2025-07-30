package org.tbk.electrum.gateway.example.watch;


import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.RateLimiter;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.common.ListAddressParams;
import org.tbk.electrum.common.WalletParams;
import org.tbk.electrum.rpc.command.ChangeGapLimitParams;
import org.tbk.electrum.rpc.command.IsSynchronizedParams;
import org.tbk.electrum.rpc.command.LoadWalletParams;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import static java.util.Objects.requireNonNull;

@Slf4j
public class ElectrumWalletWatchLoop extends AbstractScheduledService {

    @Value
    @Builder
    public static class Options {
        @NonNull
        ElectrumDaemonWalletSendBalance.Options sendBalanceOptions;

        @Nullable
        Integer gapLimit;

        public Optional<Integer> getGapLimit() {
            return Optional.ofNullable(gapLimit);
        }
    }

    private static Scheduler defaultScheduler() {
        return Scheduler.newFixedDelaySchedule(0, 10, TimeUnit.MINUTES);
    }

    private final ElectrumClient client;
    private final Scheduler scheduler;
    private final Options options;
    private final WalletParams walletParams;

    private final LongAdder counter = new LongAdder();
    private final RateLimiter logRateLimiter = RateLimiter.create(1);

    private final ElectrumDaemonWalletSendBalance task;

    public ElectrumWalletWatchLoop(ElectrumClient client, Options options) {
        this(client, options, defaultScheduler());
    }

    public ElectrumWalletWatchLoop(ElectrumClient client, Options options, Scheduler scheduler) {
        this.client = requireNonNull(client);
        this.scheduler = requireNonNull(scheduler);
        this.options = requireNonNull(options);
        this.walletParams = requireNonNull(options).getSendBalanceOptions().getWalletParams();

        this.task = new ElectrumDaemonWalletSendBalance(this.client, options.getSendBalanceOptions());
    }

    @Override
    protected void startUp() throws InterruptedException {
        this.client.loadWallet(LoadWalletParams.builder()
                .walletPath(walletParams.getWalletPath())
                .password(walletParams.getPassword().orElse(null))
                .build());

        this.options.getGapLimit().ifPresent(it -> client.changeGapLimit(ChangeGapLimitParams.builder()
                .walletPath(walletParams.getWalletPath())
                .gaplimit(it)
                .build()));

        while (!this.client.getInfo().isConnected()) {
            log.info("waiting till daemon is connected ({})", walletParams.getWalletPath());
            Thread.sleep(1000L);
        }

        while (!this.client.isWalletSynchronized(IsSynchronizedParams.builder()
                .walletPath(walletParams.getWalletPath())
                .build())) {
            log.info("waiting till wallet is synchronized ({})", walletParams.getWalletPath());
            Thread.sleep(1000L);
        }

        List<String> addresses = client.listAddresses(ListAddressParams.all(walletParams.getWalletPath()));
        log.info("start watching {} addresses in {}: {} [...]", addresses.size(), walletParams.getWalletPath(), addresses.subList(0, 3));

    }

    @Override
    protected void shutDown() {
        List<String> addresses = client.listAddresses(ListAddressParams.all(walletParams.getWalletPath()));
        log.info("stop watching {} addresses in {}: {} [...]", addresses.size(), walletParams.getWalletPath(), addresses.subList(0, 3));
    }

    @Override
    protected void runOneIteration() {
        counter.increment();

        Stopwatch sw = Stopwatch.createStarted();

        log.debug("Schedule SendBalance task on {} for {}...", LocalDateTime.now(), walletParams.getWalletPath());

        Boolean success = task.call();
        log.debug("Schedule SendBalance ended with {} after {} for {}", success, sw, walletParams.getWalletPath());

        logRateLimited(() -> log.info("Run {} completed on {} after {} for {}",
                counter.longValue(), LocalDateTime.now(), sw, walletParams.getWalletPath()));

        sw.stop();
    }

    @Override
    protected Scheduler scheduler() {
        return this.scheduler;
    }

    private void logRateLimited(Runnable runnable) {
        if (logRateLimiter.tryAcquire(60, 0, TimeUnit.SECONDS)) {
            runnable.run();
        }
    }
}
