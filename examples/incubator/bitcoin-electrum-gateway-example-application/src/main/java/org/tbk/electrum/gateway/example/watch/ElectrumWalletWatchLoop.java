package org.tbk.electrum.gateway.example.watch;


import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.ConfigKey;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import static java.util.Objects.requireNonNull;

@Slf4j
public class ElectrumWalletWatchLoop extends AbstractScheduledService {

    private static Scheduler defaultScheduler() {
        return Scheduler.newFixedDelaySchedule(0, 30, TimeUnit.MINUTES);
    }

    private final ElectrumClient client;
    private final Scheduler scheduler;

    private final LongAdder counter = new LongAdder();
    private final RateLimiter logRateLimiter = RateLimiter.create(1);

    private final ElectrumDaemonWalletSendBalance task;

    public ElectrumWalletWatchLoop(ElectrumClient client, ElectrumDaemonWalletSendBalance.Options options) {
        this(client, options, defaultScheduler());
    }

    public ElectrumWalletWatchLoop(ElectrumClient client, ElectrumDaemonWalletSendBalance.Options options, Scheduler scheduler) {
        this.client = requireNonNull(client);
        this.scheduler = requireNonNull(scheduler);

        this.task = new ElectrumDaemonWalletSendBalance(this.client, options);
    }

    @Override
    protected void startUp() throws InterruptedException {
        this.client.daemonSetConfig(ConfigKey.confirmed_only, Boolean.FALSE.toString());
        this.client.daemonSetConfig(ConfigKey.batch_rbf, Boolean.FALSE.toString());
        this.client.daemonSetConfig(ConfigKey.fee_per_kb, String.valueOf(120 * 1_024));
        this.client.daemonSetConfig(ConfigKey.dynamic_fees, Boolean.FALSE.toString());
        this.client.daemonSetConfig(ConfigKey.check_updates, Boolean.FALSE.toString());
        this.client.daemonSetConfig(ConfigKey.dont_show_testnet_warning, Boolean.TRUE.toString());
        // call to "Abstract_Wallet.get_full_history" logs every access - logs can grow quite large
        this.client.daemonSetConfig(ConfigKey.log_to_file, Boolean.FALSE.toString());

        printConfig(ConfigKey.confirmed_only);
        printConfig(ConfigKey.check_updates);
        printConfig(ConfigKey.fee_per_kb);

        List<String> addresses = client.listAddresses();
        log.info("start watching addresses: {}", addresses);

        while (!this.client.daemonStatus().isConnected()) {
            log.info("waiting till daemon is connected");
            Thread.sleep(100L);
        }

        while (!this.client.isWalletSynchronized()) {
            log.info("waiting till wallet is synchronized");
            Thread.sleep(100L);
        }
    }

    @Override
    protected void shutDown() {
        List<String> addresses = client.listAddresses();
        log.info("stop watching addresses: {}", addresses);
    }

    @Override
    protected void runOneIteration() {
        counter.increment();

        Stopwatch sw = Stopwatch.createStarted();

        log.debug("Schedule SendBalance task... on {}", LocalDateTime.now());

        Boolean success = task.call();
        log.debug("Schedule SendBalance ended with {} after {}", success, sw);

        logRateLimited(() -> log.info("Run {} completed on {} after {}",
                counter.longValue(), LocalDateTime.now(), sw));

        sw.stop();
    }

    private void printConfig(ConfigKey key) {
        log.info("config '{}': {}", key.name(), this.client.daemonGetConfig(key).orElse(null));
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