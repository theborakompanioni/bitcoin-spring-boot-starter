package org.tbk.spring.testcontainer.bitcoind.regtest;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
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

    @ConditionalOnBean({BitcoinClient.class})
    @ConditionalOnMissingBean(ScheduledBitcoindRegtestMiner.class)
    @Bean(initMethod = "startAsync", destroyMethod = "stopAsync")
    public ScheduledBitcoindRegtestMiner scheduledBitcoindRegtestMiner(BitcoinClient bitcoinJsonRpcClient,
                                                                       @Qualifier("bitcoindRegtestMinerScheduler")
                                                                               AbstractScheduledService.Scheduler scheduler) {
        return new ScheduledBitcoindRegtestMiner(bitcoinJsonRpcClient, scheduler);
    }

    @ConditionalOnMissingBean(name = "bitcoindRegtestMinerScheduler")
    @Bean("bitcoindRegtestMinerScheduler")
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
