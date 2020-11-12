package org.tbk.bitcoin.txstats.example;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.msgilligan.bitcoinj.json.pojo.BlockInfo;
import com.msgilligan.bitcoinj.json.pojo.RawTransactionInfo;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptPattern;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.context.WebServerPortFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.support.TransactionTemplate;
import org.tbk.bitcoin.common.bitcoinj.util.MoreScripts;
import org.tbk.bitcoin.common.util.ShutdownHooks;
import org.tbk.bitcoin.jsonrpc.cache.CacheFacade;
import org.tbk.bitcoin.txstats.example.cache.AppCacheFacade;
import org.tbk.bitcoin.txstats.example.model.TxScoreNeoEntity;
import org.tbk.bitcoin.txstats.example.model.TxScoreNeoRepository;
import org.tbk.bitcoin.txstats.example.score.TxScoreRunner;
import org.tbk.bitcoin.txstats.example.score.TxScoreService;
import org.tbk.bitcoin.txstats.example.score.label.ScoreLabel;
import org.tbk.bitcoin.txstats.example.util.CoinWithCurrencyConversion;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.spring.bitcoin.neo4j.model.*;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.convert.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@SpringBootApplication
public class BitcoinTxStatsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(BitcoinTxStatsApplication.class)
                .listeners(applicationPidFileWriter(), webServerPortFileWriter())
                .web(WebApplicationType.SERVLET)
                .profiles("development", "local", "demo")
                .run(args);
    }

    public static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }

    public static ApplicationListener<?> webServerPortFileWriter() {
        return new WebServerPortFileWriter("application.port");
    }

    @Bean
    @Profile("disabled-for-now-demo")
    public TxScoreRunner txScoreRunner(NetworkParameters networkParameters,
                                       MessagePublishService<Transaction> bitcoinjTransactionPublishService,
                                       TxScoreService txScoreService,
                                       AppCacheFacade caches) {
        return new TxScoreRunner(networkParameters, bitcoinjTransactionPublishService, txScoreService, caches);
    }

    @Bean
    @Profile("demo & !test")
    public CommandLineRunner mainRunner(MessagePublishService<Transaction> bitcoinjTransactionPublishService) {
        return args -> {
            log.info("Starting example application mainRunner");

            Stopwatch statsStopwatch = Stopwatch.createStarted();
            Duration statsInterval = Duration.ofSeconds(10);
            AtomicLong statsTxCounter = new AtomicLong();

            Flux.from(bitcoinjTransactionPublishService)
                    .buffer(statsInterval, Schedulers.newSingle("buffer", true))
                    .subscribe(arg -> {
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
                    });

            bitcoinjTransactionPublishService.awaitRunning(Duration.ofSeconds(10));
        };
    }

    @Bean
    @Profile("demo & !test")
    public CommandLineRunner insertBlockToNeo4j(NetworkParameters networkParameters,
                                                MessagePublishService<Block> bitcoinjBlockPublishService,
                                                MessagePublishService<Transaction> bitcoinjTranscationPublishService,
                                                BlockNeoRepository blockRepository,
                                                TxNeoRepository transactionRepository,
                                                TxOutputNeoRepository txOutputRepository,
                                                AddressNeoRepository addressRepository,
                                                TxScoreNeoRepository txScoreNeoRepository,
                                                TransactionTemplate transactionTemplate,
                                                CacheFacade caches,
                                                TxScoreService txScoreService) {
        return args -> {
            log.info("Starting example application insertBlockToNeo4j");

            ExecutorService executorService = Executors.newFixedThreadPool(10, new ThreadFactoryBuilder()
                    .setNameFormat("load-init-block-%d")
                    .setDaemon(false)
                    .build());

            Runtime.getRuntime().addShutdownHook(ShutdownHooks.shutdownHook(executorService, Duration.ofSeconds(10)));

            Function<Block, BlockNeoEntity> toBlockNeoEntity = block -> {
                BlockNeoEntity neoBlock = new BlockNeoEntity();

                neoBlock.setHash(block.getHash().toString());

                neoBlock.setVersion(block.getVersion());
                neoBlock.setMerkleroot(block.getMerkleRoot().toString());
                neoBlock.setTime(block.getTime().toInstant());
                neoBlock.setDifficulty(block.getDifficultyTarget());
                neoBlock.setNonce(block.getNonce());

                blockRepository.findById(block.getPrevBlockHash().toString())
                        .ifPresent(neoBlock::setPrevblock);

                return neoBlock;
            };

            Function<Block, BlockNeoEntity> insertNeoBlock = block -> transactionTemplate.execute(status -> {
                log.info("inserting new block {}", block.getHash());
                return blockRepository.save(toBlockNeoEntity.apply(block));
            });

            // a block with comparatively low amount of total inputs and tx.. (tx count: 318; i nput count: 875)
            Sha256Hash blockHash = Sha256Hash.wrap("000000000000000000341c2bcc0e2eadb0a4b1453a44ac31cab893080f967a85");
            Block block = caches.block().getUnchecked(blockHash);
            Block prevBlock = caches.block().getUnchecked(block.getPrevBlockHash());
            Block prevPrevBlock = caches.block().getUnchecked(prevBlock.getPrevBlockHash());

            BlockNeoEntity savedPrevPrevNeoBlock = insertNeoBlock.apply(prevPrevBlock);
            BlockNeoEntity savedPrevNeoBlock = insertNeoBlock.apply(prevBlock);
            BlockNeoEntity savedNeoBlock = insertNeoBlock.apply(block);

            AtomicLong txCounter = new AtomicLong();
            block.getTransactions().forEach(tx -> {
                TxNeoEntity newNeoTx = transactionTemplate.execute(status -> {
                    String txId = tx.getTxId().toString();

                    log.info("{} - inserting new transaction {}", txCounter.incrementAndGet(), txId);

                    TxNeoEntity neoTx = new TxNeoEntity();
                    neoTx.setTxid(txId);
                    neoTx.setVersion(tx.getVersion());
                    neoTx.setTxincount(tx.getInputs().size());
                    neoTx.setTxoutcount(tx.getOutputs().size());
                    neoTx.setLocktime(tx.getLockTime());
                    neoTx.setBlock(savedNeoBlock);

                    List<TxOutputNeoEntity> neoSpentOutputs = Lists.newArrayList();
                    tx.getInputs().forEach(input -> {
                        if (input.isCoinBase()) {
                            // coinbase inputs cannot be fetched
                            // via `getrawtransaction`
                            return;
                        }

                        TransactionOutPoint outpoint = input.getOutpoint();

                        String neoTxoId = outpoint.getHash().toString() + ":" + outpoint.getIndex();
                        TxOutputNeoEntity txOutputNeoEntitySpent = txOutputRepository.findById(neoTxoId).orElseGet(() -> {
                            Transaction txFromInput = caches.tx().getUnchecked(outpoint.getHash());
                            TransactionOutput output = txFromInput.getOutput(outpoint.getIndex());

                            TxOutputNeoEntity neoTxo = new TxOutputNeoEntity();
                            neoTxo.setId(neoTxoId);
                            neoTxo.setIndex(output.getIndex());
                            neoTxo.setValue(output.getValue().getValue());
                            neoTxo.setSize(output.getScriptBytes().length);

                            String scriptTypeName = Optional.ofNullable(output.getScriptPubKey())
                                    .map(Script::getScriptType)
                                    .map(Enum::name)
                                    .or(() -> Optional.ofNullable(output.getScriptPubKey())
                                            .filter(ScriptPattern::isOpReturn)
                                            .map(val -> "opreturn"))
                                    .orElse("unknown")
                                    .toLowerCase();

                            boolean isOpReturn = ScriptPattern.isOpReturn(output.getScriptPubKey());
                            neoTxo.setMeta(ImmutableMap.<String, Object>builder()
                                    .put("op_return_data", isOpReturn ? output.getScriptPubKey().toString() : "")
                                    .put("script_type_name", scriptTypeName)
                                    .put("is_sent_to_multisig", ScriptPattern.isSentToMultisig(output.getScriptPubKey()))
                                    .put("is_witness_commitment", ScriptPattern.isWitnessCommitment(output.getScriptPubKey()))
                                    .build());

                            Optional<Address> addressOrEmpty = MoreScripts.extractAddress(networkParameters, output.getScriptPubKey());
                            addressOrEmpty.ifPresent(address -> {
                                AddressNeoEntity AddressNeoEntity = addressRepository.findById(address.toString()).orElseGet(() -> {
                                    AddressNeoEntity newAddressNeoEntity = new AddressNeoEntity();
                                    newAddressNeoEntity.setAddress(address.toString());
                                    return addressRepository.save(newAddressNeoEntity);
                                });

                                neoTxo.setAddress(AddressNeoEntity);
                            });

                            return txOutputRepository.save(neoTxo);
                        });

                        neoSpentOutputs.add(txOutputNeoEntitySpent);
                    });

                    List<TxOutputNeoEntity> neoCreatedOutputs = Lists.newArrayList();
                    tx.getOutputs().forEach(output -> {
                        TxOutputNeoEntity neoTxo = new TxOutputNeoEntity();
                        neoTxo.setId(txId + ":" + output.getIndex());
                        neoTxo.setIndex(output.getIndex());
                        neoTxo.setValue(output.getValue().getValue());
                        neoTxo.setCreatedIn(neoTx);
                        neoTxo.setSize(output.getScriptBytes().length);


                        String scriptTypeName = Optional.ofNullable(output.getScriptPubKey())
                                .map(Script::getScriptType)
                                .map(Enum::name)
                                .or(() -> Optional.ofNullable(output.getScriptPubKey())
                                        .filter(ScriptPattern::isOpReturn)
                                        .map(val -> "opreturn"))
                                .orElse("unknown")
                                .toLowerCase();

                        boolean isOpReturn = ScriptPattern.isOpReturn(output.getScriptPubKey());
                        neoTxo.setMeta(ImmutableMap.<String, Object>builder()
                                .put("op_return_data", isOpReturn ? output.getScriptPubKey().toString() : "")
                                .put("script_type_name", scriptTypeName)
                                .put("is_sent_to_multisig", ScriptPattern.isSentToMultisig(output.getScriptPubKey()))
                                .put("is_witness_commitment", ScriptPattern.isWitnessCommitment(output.getScriptPubKey()))
                                .build());

                        Optional<Address> addressOrEmpty = MoreScripts.extractAddress(networkParameters, output.getScriptPubKey());
                        addressOrEmpty.ifPresent(address -> {
                            AddressNeoEntity AddressNeoEntity = addressRepository.findById(address.toString()).orElseGet(() -> {
                                AddressNeoEntity newAddressNeoEntity = new AddressNeoEntity();
                                newAddressNeoEntity.setAddress(address.toString());
                                return addressRepository.save(newAddressNeoEntity);
                            });

                            neoTxo.setAddress(AddressNeoEntity);
                        });

                        TxOutputNeoEntity savedNeoTxo = txOutputRepository.save(neoTxo);
                        neoCreatedOutputs.add(savedNeoTxo);
                    });

                    neoTx.setInputs(neoSpentOutputs);
                    neoTx.setOutputs(neoCreatedOutputs);

                    Coin fee = Optional.of(tx)
                            .filter(val -> !val.isCoinBase())
                            .map(t -> {
                                Coin inputSum = t.getInputs().stream()
                                        .filter(val -> !val.isCoinBase())
                                        .map(TransactionInput::getOutpoint)
                                        .map(outpoint -> {
                                            Transaction txFromInput = caches.tx().getUnchecked(outpoint.getHash());
                                            return txFromInput.getOutput(outpoint.getIndex());
                                        })
                                        .map(TransactionOutput::getValue)
                                        .reduce(Coin.ZERO, Coin::add);

                                return Optional.of(inputSum.minus(t.getOutputSum()))
                                        .filter(Coin::isPositive)
                                        .orElse(Coin.ZERO);
                            }).orElse(Coin.ZERO);

                    neoTx.setMeta(ImmutableMap.<String, Object>builder()
                            .put("fee", fee.getValue())
                            .build());

                    return transactionRepository.save(neoTx);
                });
            });

            Flux.fromIterable(block.getTransactions())
                    .doOnNext(val -> {
                        log.info("Score check for tx {}", val.getTxId());
                    })
                    .flatMap(txScoreService::scoreTransaction)
                    .subscribe(score -> {
                        String txId = score.getTx().getTxId().toString();

                        List<String> labels = score.getLabels().stream()
                                .map(ScoreLabel::getName)
                                .collect(Collectors.toList());

                        TxScoreNeoEntity newNeoTxScore = transactionTemplate.execute(status -> {
                            TxScoreNeoEntity neoTxScore = new TxScoreNeoEntity();

                            neoTxScore.setCreatedAt(score.getCreatedAt());
                            neoTxScore.setFinalized(score.isFinalized());
                            neoTxScore.setScore(score.getScore());
                            neoTxScore.setType(score.getType().toString());
                            neoTxScore.setLabels(labels);

                            transactionRepository.findById(txId).ifPresent(neoTx -> {
                                neoTxScore.setTx(neoTx);
                            });

                            return txScoreNeoRepository.save(neoTxScore);
                        });
                        log.info("Score check finished for tx {} - {}", txId, newNeoTxScore.getId());
                    });
        };
    }

    @Bean
    @Profile("disable-for-now")
    public CommandLineRunner txStatsDemoRunner(NetworkParameters networkParameters,
                                               MessagePublishService<Transaction> bitcoinjTransactionPublishService,
                                               AppCacheFacade caches) {
        CurrencyUnit btcCurrencyUnit = Monetary.getCurrency("BTC");
        ConversionQuery conversionQuery = ConversionQueryBuilder.of()
                .setBaseCurrency(btcCurrencyUnit)
                .setTermCurrency(Monetary.getCurrency("USD"))
                .build();

        return args -> {
            log.info("Starting example application txStatsDemoRunner");
            /* Flux.from(bitcoinjTransactionPublishService)
                    .publishOn(Schedulers.elastic())
                    .subscribe(tx -> {
                                log.info("{}", tx);
                            }); */
            AtomicLong totalRuns = new AtomicLong(0L);

            ExecutorService executorService = Executors.newFixedThreadPool(100, new ThreadFactoryBuilder()
                    .setNameFormat("load-tx-cache-%d")
                    .setDaemon(false)
                    .build());

            Runtime.getRuntime().addShutdownHook(ShutdownHooks.shutdownHook(executorService, Duration.ofSeconds(10)));

            Flux.from(bitcoinjTransactionPublishService)
                    .parallel()
                    .runOn(Schedulers.fromExecutorService(executorService))
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
                            Transaction txFromInput = caches.tx().getUnchecked(outpoint.getHash());

                            RawTransactionInfo txFromInputInfo = caches.txInfo().getUnchecked(txFromInput.getTxId());

                            Optional.ofNullable(txFromInputInfo.getBlockhash())
                                    .map(caches.blockInfo()::getUnchecked);
                        }
                        log.info("loading data took {} for tx {}", stopwatch.stop(), tx.getTxId());
                    })
                    .sequential()
                    .onErrorContinue((throwable, causingValue) -> {
                        log.error("error while handling " + causingValue, throwable);
                    })
                    .subscribe(tx -> {
                        log.info("======================================================");

                        try {
                            CurrencyConversion btcToUsdConversion = caches.currencyConversion().getUnchecked(conversionQuery);


                            log.info("run: {}", totalRuns.incrementAndGet());
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
                                Transaction txFromInput = caches.tx().getUnchecked(outpoint.getHash());

                                RawTransactionInfo txFromInputInfo = caches.txInfo().getUnchecked(txFromInput.getTxId());

                                Optional<BlockInfo> blockInfo = Optional.ofNullable(txFromInputInfo.getBlockhash())
                                        .map(caches.blockInfo()::getUnchecked);

                                TransactionOutput fromOutput = txFromInput.getOutput(outpoint.getIndex());
                                Script scriptPubKey = fromOutput.getScriptPubKey();
                                String fromAddress = MoreScripts.extractAddress(networkParameters, scriptPubKey)
                                        .map(Object::toString)
                                        .orElseGet(() -> "none");

                                CoinWithCurrencyConversion inWithFiatAmount = CoinWithCurrencyConversion.builder()
                                        .coin(fromOutput.getValue())
                                        .currencyConversion(btcToUsdConversion)
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
                                        .currencyConversion(btcToUsdConversion)
                                        .build();

                                log.info("out: {} {} {}", scriptPubKey.getScriptType(), toAddress, outWithFiatAmount.toFriendlyString());
                            }

                            // coinbase tx has no fee. if fee is "negative" -> assume zero
                            Coin fee = Optional.of(inputSum.minus(tx.getOutputSum()))
                                    .filter(Coin::isPositive)
                                    .orElse(Coin.ZERO);

                            CoinWithCurrencyConversion feeWithFiatAmount = CoinWithCurrencyConversion.builder()
                                    .coin(fee)
                                    .currencyConversion(btcToUsdConversion)
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
