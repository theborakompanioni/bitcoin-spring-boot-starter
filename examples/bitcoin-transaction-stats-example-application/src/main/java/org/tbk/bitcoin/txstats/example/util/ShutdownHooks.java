package org.tbk.bitcoin.txstats.example.util;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class ShutdownHooks {

    private ShutdownHooks() {
        throw new UnsupportedOperationException();
    }

    public static Thread shutdownHook(ExecutorService executor, Duration duration) {
        return new Thread(() -> {
            try {
                executor.shutdown();
                boolean success = executor.awaitTermination(duration.toMillis(), TimeUnit.MILLISECONDS);
                if (!success) {
                    List<Runnable> runnables = executor.shutdownNow();
                    if (!runnables.isEmpty()) {
                        log.error("Could await " + runnables.size() + " tasks from terminating");
                    }
                }
            } catch (InterruptedException e) {
                List<Runnable> runnables = executor.shutdownNow();
                log.error("Could await " + runnables.size() + " tasks from terminating", e);
            }
        });
    }
}
