package org.tbk.bitcoin.regtest.config;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;
import org.consensusj.bitcoin.rpc.BitcoinClient;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.regtest.mining.*;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RegtestMiner.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.regtest.mining.enabled", havingValue = "true")
@AutoConfigureAfter(BitcoinRegtestAutoConfiguration.class)
public class BitcoinRegtestMiningAutoConfiguration {

    private final BitcoinRegtestMiningProperties properties;

    public BitcoinRegtestMiningAutoConfiguration(BitcoinRegtestAutoConfigProperties properties) {
        this.properties = requireNonNull(properties.getMining());
    }

    @Bean
    @ConditionalOnBean({BitcoinClient.class})
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "org.tbk.bitcoin.regtest.mining.coinbase-reward-address")
    public CoinbaseRewardAddressSupplier staticCoinbaseRewardAddressSupplier(BitcoinClient bitcoinJsonRpcClient) {
        return this.properties.getCoinbaseRewardAddress()
                .map(it -> Address.fromString(bitcoinJsonRpcClient.getNetParams(), it))
                .map(StaticCoinbaseRewardAddressSupplier::new)
                .orElseThrow(() -> new IllegalStateException("Cannot create CoinbaseRewardAddressSupplier from static address"));
    }

    @Bean
    @ConditionalOnBean({BitcoinClient.class})
    @ConditionalOnMissingBean
    public CoinbaseRewardAddressSupplier bitcoinClientCoinbaseRewardAddressSupplier(BitcoinClient bitcoinJsonRpcClient) {
        return new BitcoinClientCoinbaseRewardAddressSupplier(bitcoinJsonRpcClient);
    }

    @Bean
    @ConditionalOnBean({BitcoinClient.class})
    @ConditionalOnMissingBean
    public RegtestMiner regtestMiner(BitcoinClient bitcoinJsonRpcClient,
                                     CoinbaseRewardAddressSupplier coinbaseRewardAddressSupplier) {
        return new RegtestMinerImpl(bitcoinJsonRpcClient, coinbaseRewardAddressSupplier);
    }

    @Bean("regtestMinerScheduler")
    @ConditionalOnMissingBean(name = "regtestMinerScheduler")
    public MinMaxDurationScheduler regtestMinerScheduler() {
        Duration minDuration = properties.getNextBlockDuration().getMinDuration();
        Duration maxDuration = properties.getNextBlockDuration().getMaxDuration();

        log.debug("Create scheduler that periodically mines blocks between {} and {}", minDuration, maxDuration);

        return MinMaxDurationScheduler.builder()
                .minDuration(minDuration)
                .maxDuration(maxDuration)
                .build();
    }

    @Bean(destroyMethod = "stopAsync")
    @ConditionalOnBean({
            RegtestMiner.class,
            MinMaxDurationScheduler.class
    })
    @ConditionalOnMissingBean(ScheduledRegtestMiner.class)
    @ConditionalOnProperty(value = "org.tbk.bitcoin.regtest.mining.scheduled-mining-enabled", havingValue = "true", matchIfMissing = true)
    public ScheduledRegtestMiner scheduledregtestMiner(RegtestMiner regtestMiner,
                                                       @Qualifier("regtestMinerScheduler") Scheduler scheduler) {
        ScheduledRegtestMiner scheduledregtestMiner = new ScheduledRegtestMiner(regtestMiner, scheduler);
        scheduledregtestMiner.startAsync();
        return scheduledregtestMiner;
    }

    @Bean
    @ConditionalOnBean({RegtestMiner.class})
    public InitializingBean regtestMinerPreminer(RegtestMiner regtestMiner) {
        int numberOfBlocksToMine = properties.getMineInitialAmountOfBlocks();

        if (numberOfBlocksToMine == 0) {
            return () -> {
                log.debug("Will not mine initial number of blocks as 'mine-initial-amount-of-blocks' is zero.");
            };
        }

        return () -> {
            log.info("Will mine an initial number of {} blocks.", numberOfBlocksToMine);

            Stopwatch stopwatch = Stopwatch.createStarted();

            List<Sha256Hash> blockHashes = regtestMiner.mineBlocks(numberOfBlocksToMine);

            log.info("Mined initial number of {} blocks in {}", blockHashes.size(), stopwatch);
            stopwatch.stop();
        };
    }

    public static class MinMaxDurationScheduler extends AbstractScheduledService.CustomScheduler {
        private static final SecureRandom random = new SecureRandom();

        private final Duration minDuration;

        private final Duration maxDuration;

        @Builder
        public MinMaxDurationScheduler(@NonNull Duration minDuration, @NonNull Duration maxDuration) {
            this.minDuration = requireNonNull(minDuration);
            this.maxDuration = requireNonNull(maxDuration);

            checkArgument(!minDuration.isNegative(), "'minDuration' must be positive or zero");
            checkArgument(!maxDuration.isNegative(), "'maxDuration' must be positive");
            checkArgument(!maxDuration.isZero(), "'maxDuration' must be positive");
            checkArgument(maxDuration.compareTo(minDuration) >= 0, "'maxDuration' must be greater or equal to 'minDuration'");
        }

        @Override
        protected Schedule getNextSchedule() {
            long minMillis = minDuration.toMillis();
            long maxMillis = maxDuration.toMillis();

            long randomMillis = (long) Math.max(
                    minMillis,
                    minMillis + (random.nextDouble() * (maxMillis - minMillis))
            );

            Duration durationTillNewBlock = Duration.ofMillis(randomMillis);

            log.debug("Duration till next block: {}", durationTillNewBlock);

            return new Schedule(durationTillNewBlock.toSeconds(), TimeUnit.SECONDS);

        }
    }
}
