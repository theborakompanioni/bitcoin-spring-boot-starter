package org.tbk.bitcoin.txstats.example;

import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.msgilligan.bitcoinj.json.pojo.BlockInfo;
import com.msgilligan.bitcoinj.json.pojo.RawTransactionInfo;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.context.WebServerPortFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.tbk.bitcoin.txstats.example.util.CoinWithCurrencyConversion;
import org.tbk.bitcoin.txstats.example.util.MoreScripts;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.convert.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

@Slf4j
@SpringBootApplication
public class BitcoinTxStatsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(BitcoinTxStatsApplication.class)
                .listeners(applicationPidFileWriter(), webServerPortFileWriter())
                .web(WebApplicationType.SERVLET)
                .profiles("development", "local")
                .run(args);
    }

    public static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }

    public static ApplicationListener<?> webServerPortFileWriter() {
        return new WebServerPortFileWriter("application.port");
    }

    @Bean
    public CommandLineRunner mainRunner(MessagePublishService<Transaction> bitcoinjTransactionPublishService) {
        return args -> {
            log.info("Starting example application mainRunner");

            Stopwatch statsStopwatch = Stopwatch.createStarted();
            Duration statsInterval = Duration.ofSeconds(10);
            AtomicLong statsTxCounter = new AtomicLong();

            Flux.from(bitcoinjTransactionPublishService)
                    .buffer(statsInterval)
                    .doOnNext(arg -> {
                        statsTxCounter.addAndGet(arg.size());

                        long intervalElapsedSeconds = Math.max(statsInterval.toSeconds(), 1);
                        long statsTotalElapsedSeconds = Math.max(statsStopwatch.elapsed(TimeUnit.SECONDS), 1);

                        log.info("=======================================");
                        log.info("elapsed: {}", statsStopwatch);
                        log.info("tx count last {} seconds: {}", intervalElapsedSeconds, arg.size());
                        log.info("tx/s last {} seconds: {}", intervalElapsedSeconds, arg.size() / (float) intervalElapsedSeconds);
                        log.info("total tx count: {}", statsTxCounter.get());
                        log.info("total tx/s: {}", statsTxCounter.get() / (float) statsTotalElapsedSeconds);
                        log.info("=======================================");
                    }).subscribe();

            bitcoinjTransactionPublishService.awaitRunning(Duration.ofSeconds(10));
        };
    }

    @Bean
    public CommandLineRunner txStatsDemoRunner(NetworkParameters networkParameters,
                                               BitcoinClient bitcoinClient,
                                               MessagePublishService<Transaction> bitcoinjTransactionPublishService) {
        CurrencyUnit btcCurrencyUnit = Monetary.getCurrency("BTC");
        ConversionQuery conversionQuery = ConversionQueryBuilder.of()
                .setBaseCurrency(btcCurrencyUnit)
                .setTermCurrency(Monetary.getCurrency("USD"))
                .build();

        LoadingCache<Sha256Hash, Transaction> txCache = CacheBuilder.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(30))
                .maximumSize(10_000)
                .build(new CacheLoader<>() {
                    @Override
                    public Transaction load(Sha256Hash key) throws Exception {
                        return bitcoinClient.getRawTransaction(key);
                    }
                });

        LoadingCache<Sha256Hash, RawTransactionInfo> txRawInfoCache = CacheBuilder.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(30))
                .maximumSize(10_000)
                .build(new CacheLoader<>() {
                    @Override
                    public RawTransactionInfo load(Sha256Hash key) throws Exception {
                        return bitcoinClient.getRawTransactionInfo(key);
                    }
                });


        LoadingCache<Sha256Hash, BlockInfo> blockInfoCache = CacheBuilder.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(30))
                .maximumSize(10_000)
                .build(new CacheLoader<>() {
                    @Override
                    public BlockInfo load(Sha256Hash key) throws Exception {
                        return bitcoinClient.getBlockInfo(key);
                    }
                });

        Supplier<CurrencyConversion> btcToUsdConversionSupplier = Suppliers.memoizeWithExpiration(() -> {
            return MonetaryConversions.getConversion(conversionQuery);
        }, 1, TimeUnit.MINUTES);

        return args -> {
            log.info("Starting example application txStatsDemoRunner");
            /*Flux.from(FlowAdapters.toPublisher(bitcoinjTransactionPublishService))
                    .publishOn(Schedulers.elastic())
                    .subscribe(tx -> {
                                log.info("{}", tx);
                            });*/

            Flux.from(bitcoinjTransactionPublishService)
                    .parallel()
                    .runOn(Schedulers.parallel())
                    .doOnNext(tx -> {
                        Stopwatch stopwatch = Stopwatch.createStarted();
                        log.info("loading data for tx {}", tx.getTxId());

                        for (TransactionInput input : tx.getInputs()) {
                            if (input.isCoinBase()) {
                                // coinbase inputs cannot be fetched
                                // via `getrawtransaction`
                                continue;
                            }
                            TransactionOutPoint outpoint = input.getOutpoint();
                            Transaction txFromInput = txCache.getUnchecked(outpoint.getHash());

                            RawTransactionInfo txFromInputInfo = txRawInfoCache.getUnchecked(txFromInput.getTxId());

                            Optional.ofNullable(txFromInputInfo.getBlockhash())
                                    .map(blockInfoCache::getUnchecked);
                        }
                        log.info("loading data took {} for tx {}", stopwatch.stop(), tx.getTxId());
                    })
                    .sequential()
                    .subscribe(tx -> {
                        log.info("======================================================");

                        try {
                            log.info("txId: {}", tx.getTxId());
                            log.info("vin count / vout count: {} / {}", tx.getInputs().size(), tx.getOutputs().size());
                            log.info("is coinbase? {}", tx.isCoinBase());
                            log.info("has witness data? {} ", tx.hasWitnesses());

                            LongAdder satoshiMinutesDestroyedTotal = new LongAdder();

                            Coin inputSum = Coin.ZERO;
                            for (TransactionInput input : tx.getInputs()) {
                                if (input.isCoinBase()) {
                                    // coinbase inputs cannot be fetched
                                    // via `getrawtransaction`
                                    continue;
                                }
                                TransactionOutPoint outpoint = input.getOutpoint();
                                Transaction txFromInput = txCache.getUnchecked(outpoint.getHash());

                                RawTransactionInfo txFromInputInfo = txRawInfoCache.getUnchecked(txFromInput.getTxId());

                                Optional<BlockInfo> blockInfo = Optional.ofNullable(txFromInputInfo.getBlockhash())
                                        .map(blockInfoCache::getUnchecked);

                                TransactionOutput fromOutput = txFromInput.getOutput(outpoint.getIndex());
                                Script scriptPubKey = fromOutput.getScriptPubKey();
                                String fromAddress = MoreScripts.extractAddress(networkParameters, scriptPubKey)
                                        .map(Object::toString)
                                        .orElseGet(() -> "none");

                                CoinWithCurrencyConversion inWithFiatAmount = CoinWithCurrencyConversion.builder()
                                        .coin(fromOutput.getValue())
                                        .currencyConversion(btcToUsdConversionSupplier.get())
                                        .build();

                                long confirmations = blockInfo
                                        .map(BlockInfo::getConfirmations)
                                        .orElse(0);

                                long satoshiMinutesDestroyed = blockInfo
                                        .map(BlockInfo::getConfirmations)
                                        .map(val -> 10 * val)
                                        .map(minutes -> fromOutput.getValue().getValue() * minutes)
                                        .orElse(0L);
                                satoshiMinutesDestroyedTotal.add(satoshiMinutesDestroyed);

                                log.info("in: {} {} {} (confirmations: {})", scriptPubKey.getScriptType(), fromAddress,
                                        inWithFiatAmount.toFriendlyString(), confirmations);

                                inputSum = inputSum.add(fromOutput.getValue());
                            }

                            for (TransactionOutput output : tx.getOutputs()) {
                                Script scriptPubKey = output.getScriptPubKey();
                                String toAddress = MoreScripts.extractAddress(networkParameters, scriptPubKey)
                                        .map(Object::toString)
                                        .orElseGet(() -> "none");

                                CoinWithCurrencyConversion outWithFiatAmount = CoinWithCurrencyConversion.builder()
                                        .coin(output.getValue())
                                        .currencyConversion(btcToUsdConversionSupplier.get())
                                        .build();

                                log.info("out: {} {} {}", scriptPubKey.getScriptType(), toAddress, outWithFiatAmount.toFriendlyString());
                            }

                            // coinbase tx has no fee. if fee is "negative" -> assume zero
                            Coin fee = Optional.of(inputSum.minus(tx.getOutputSum()))
                                    .filter(Coin::isPositive)
                                    .orElse(Coin.ZERO);

                            CoinWithCurrencyConversion feeWithFiatAmount = CoinWithCurrencyConversion.builder()
                                    .coin(fee)
                                    .currencyConversion(btcToUsdConversionSupplier.get())
                                    .build();
                            log.info("fee: {}", feeWithFiatAmount.toFriendlyString());

                            BigDecimal coindaysDestroyed = BigDecimal.valueOf(satoshiMinutesDestroyedTotal.sum())
                                    .divide(BigDecimal.TEN.pow(8), RoundingMode.HALF_UP)
                                    .divide(BigDecimal.valueOf(60 * 24), RoundingMode.HALF_UP);
                            log.info("coindays destroyed: {}", coindaysDestroyed
                                    .setScale(2, RoundingMode.HALF_UP)
                                    .toPlainString());

                        } catch (Exception e) {
                            log.error("", e);
                        }
                        log.info("======================================================");
                    });

            bitcoinjTransactionPublishService.awaitRunning(Duration.ofSeconds(10));
        };
    }

    @Bean
    @Profile("disable-for-now")
    public CommandLineRunner exchangeRateDemoRunner() {
        ConversionQuery conversionQuery = ConversionQueryBuilder.of()
                .setBaseCurrency(Monetary.getCurrency("BTC"))
                .setTermCurrency(Monetary.getCurrency("USD"))
                .build();

        return args -> {
            Flux.interval(Duration.ofSeconds(30))
                    .subscribe(foo -> {
                        log.info("======================================================");

                        ExchangeRateProvider exchangeRateProvider = MonetaryConversions.getExchangeRateProvider(conversionQuery);
                        ExchangeRate exchangeRate = exchangeRateProvider.getExchangeRate(conversionQuery);

                        log.info("exchangeRate: {}", exchangeRate);
                        log.info("======================================================");
                    });
        };
    }

}
