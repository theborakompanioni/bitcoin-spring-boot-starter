package org.tbk.spring.testcontainer.bitcoind.regtest;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@EnableConfigurationProperties(BitcoindRegtestMinerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.testcontainer.bitcoind-regtest-miner.enabled", havingValue = "true")
public class BitcoindRegtestMinerAutoConfiguration {

    private final BitcoindRegtestMinerProperties properties;

    public BitcoindRegtestMinerAutoConfiguration(BitcoindRegtestMinerProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnBean({BitcoinClient.class})
    @ConditionalOnMissingBean(CoinbaseRewardAddressSupplier.class)
    @ConditionalOnProperty(value = "org.tbk.spring.testcontainer.bitcoind-regtest-miner.coinbase-reward-address")
    public CoinbaseRewardAddressSupplier staticCoinbaseRewardAddressSupplier(BitcoinClient bitcoinJsonRpcClient) {
        return this.properties.getCoinbaseRewardAddress()
                .map(it -> Address.fromString(bitcoinJsonRpcClient.getNetParams(), it))
                .map(StaticCoinbaseRewardAddressSupplier::new)
                .orElseThrow(() -> new IllegalStateException("Cannot create CoinbaseRewardAddressSupplier from static address"));
    }

    @Bean
    @ConditionalOnBean({BitcoinClient.class})
    @ConditionalOnMissingBean(CoinbaseRewardAddressSupplier.class)
    public CoinbaseRewardAddressSupplier bitcoinClientCoinbaseRewardAddressSupplier(BitcoinClient bitcoinJsonRpcClient) {
        return new BitcoinClientCoinbaseRewardAddressSupplier(bitcoinJsonRpcClient);
    }

    @Bean(initMethod = "startAsync", destroyMethod = "stopAsync")
    @ConditionalOnBean({BitcoinClient.class})
    @ConditionalOnMissingBean(ScheduledBitcoindRegtestMiner.class)
    public ScheduledBitcoindRegtestMiner scheduledBitcoindRegtestMiner(BitcoinClient bitcoinJsonRpcClient,
                                                                       @Qualifier("bitcoindRegtestMinerScheduler")
                                                                               AbstractScheduledService.Scheduler scheduler,
                                                                       CoinbaseRewardAddressSupplier coinbaseRewardAddressSupplier) {
        return new ScheduledBitcoindRegtestMiner(bitcoinJsonRpcClient, scheduler, coinbaseRewardAddressSupplier);
    }

    @Bean
    @ConditionalOnBean({ScheduledBitcoindRegtestMiner.class})
    public InitializingBean scheduledBitcoindRegtestMinerPreminer(ScheduledBitcoindRegtestMiner scheduledBitcoindRegtestMiner) {

        int numberOfBlocksToMine = properties.getMineInitialAmountOfBlocks();

        if (numberOfBlocksToMine == 0) {
            return () -> {
                log.debug("Will not mine initial number of blocks as 'numberOfBlocksToMine' is zero.");
            };
        }

        return () -> {
            log.info("Will mine an initial number of {} blocks.", numberOfBlocksToMine);

            Stopwatch stopwatch = Stopwatch.createStarted();

            scheduledBitcoindRegtestMiner.awaitRunning();

            int counter = 0;
            while (counter < numberOfBlocksToMine) {
                scheduledBitcoindRegtestMiner.runOneIteration();
                counter++;
            }

            log.info("Mined initial number of {} blocks in {}", counter, stopwatch);
            stopwatch.stop();
        };
    }


    @Bean("bitcoindRegtestMinerScheduler")
    @ConditionalOnMissingBean(name = "bitcoindRegtestMinerScheduler")
    public AbstractScheduledService.Scheduler bitcoindRegtestMinerScheduler() {
        Duration minDuration = properties.getNextBlockDuration().getMinDuration();
        Duration maxDuration = properties.getNextBlockDuration().getMaxDuration();

        log.debug("Create scheduler that periodically mines blocks between {} and {}", minDuration, maxDuration);

        return MinMaxDurationScheduler.builder()
                .minDuration(minDuration)
                .maxDuration(maxDuration)
                .build();
    }

    public static class MinMaxDurationScheduler extends AbstractScheduledService.CustomScheduler {
        private final Duration minDuration;

        private final Duration maxDuration;

        @Builder
        public MinMaxDurationScheduler(@NonNull Duration minDuration, @NonNull Duration maxDuration) {
            checkArgument(!minDuration.isNegative(), "'minDuration' must be positive or zero");
            checkArgument(!maxDuration.isNegative(), "'maxDuration' must be positive");
            checkArgument(!maxDuration.isZero(), "'maxDuration' must be positive");
            checkArgument(maxDuration.compareTo(minDuration) >= 0, "'maxDuration' must be greater or equal to 'minDuration'");

            this.minDuration = requireNonNull(minDuration);
            this.maxDuration = requireNonNull(maxDuration);
        }

        @Override
        protected Schedule getNextSchedule() throws Exception {
            long minMillis = minDuration.toMillis();
            long maxMillis = maxDuration.toMillis();

            long randomMillis = (long) Math.max(
                    minMillis,
                    minMillis + (Math.random() * (maxMillis - minMillis))
            );

            Duration durationTillNewBlock = Duration.ofMillis(randomMillis);

            log.debug("Duration till next block: {}", durationTillNewBlock);

            return new Schedule(durationTillNewBlock.toSeconds(), TimeUnit.SECONDS);

        }
    }
}
