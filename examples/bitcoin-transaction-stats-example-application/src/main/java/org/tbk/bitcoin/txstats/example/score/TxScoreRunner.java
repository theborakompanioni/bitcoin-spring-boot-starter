package org.tbk.bitcoin.txstats.example.score;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.springframework.boot.CommandLineRunner;
import org.tbk.bitcoin.txstats.example.cache.CacheFacade;
import org.tbk.bitcoin.txstats.example.util.ShutdownHooks;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.convert.ConversionQuery;
import javax.money.convert.ConversionQueryBuilder;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Objects.requireNonNull;

@Slf4j
public class TxScoreRunner implements CommandLineRunner {
    private final NetworkParameters networkParameters;
    private final MessagePublishService<Transaction> bitcoinjTransactionPublishService;
    private final TxScoreService txScoreService;
    private final CacheFacade caches;

    public TxScoreRunner(NetworkParameters networkParameters,
                         MessagePublishService<Transaction> bitcoinjTransactionPublishService,
                         TxScoreService txScoreService,
                         CacheFacade caches) {
        this.networkParameters = requireNonNull(networkParameters);
        this.bitcoinjTransactionPublishService = requireNonNull(bitcoinjTransactionPublishService);
        this.txScoreService = requireNonNull(txScoreService);
        this.caches = requireNonNull(caches);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting example TxScoreRunner");

        CurrencyUnit btcCurrencyUnit = Monetary.getCurrency("BTC");
        ConversionQuery conversionQuery = ConversionQueryBuilder.of()
                .setBaseCurrency(btcCurrencyUnit)
                .setTermCurrency(Monetary.getCurrency("USD"))
                .build();

        ExecutorService executorService = Executors.newFixedThreadPool(100, new ThreadFactoryBuilder()
                .setNameFormat("tx-score-%d")
                .setDaemon(false)
                .build());

        Runtime.getRuntime().addShutdownHook(ShutdownHooks.shutdownHook(executorService, Duration.ofSeconds(10)));

        AtomicLong totalRuns = new AtomicLong(0L);
        Flux.from(bitcoinjTransactionPublishService)
                .parallel()
                .runOn(Schedulers.fromExecutorService(executorService))
                .flatMap(tx -> {
                    log.info("Score check for tx {}", tx.getTxId());
                    return txScoreService.scoreTransaction(tx);
                })
                .sequential()
                .onErrorContinue((throwable, causingValue) -> {
                    log.error("error while handling " + causingValue, throwable);
                })
                .subscribe(scoredTx -> {
                    log.info("======================================================");

                    log.info("run: {}", totalRuns.incrementAndGet());
                    log.info("txId: {}", scoredTx.getTx().getTxId());
                    log.info("type: {}", scoredTx.getType());
                    log.info("score: {}", scoredTx.getScore());
                    log.info("labels: {}", scoredTx.getLabels());
                    log.info("======================================================");
                });

        bitcoinjTransactionPublishService.awaitRunning(Duration.ofSeconds(10));
    }
}
