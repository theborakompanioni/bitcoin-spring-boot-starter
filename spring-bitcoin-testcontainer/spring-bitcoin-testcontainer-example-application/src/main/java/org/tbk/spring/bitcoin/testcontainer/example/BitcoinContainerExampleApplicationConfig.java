package org.tbk.spring.bitcoin.testcontainer.example;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.spring.bitcoin.testcontainer.example.regtest.ScheduledBitcoinContainerRegtestMiner;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class BitcoinContainerExampleApplicationConfig {

    @Bean(initMethod = "startAsync", destroyMethod = "stopAsync")
    public ScheduledBitcoinContainerRegtestMiner bitcoinRegtestContainerMiner(BitcoinClient bitcoinJsonRpcClient,
                                                                              @Qualifier("bitcoinRegtestBlockMiningScheduler")
                                                                                      AbstractScheduledService.Scheduler scheduler) {
        return new ScheduledBitcoinContainerRegtestMiner(bitcoinJsonRpcClient, scheduler);
    }

    @Bean("bitcoinRegtestBlockMiningScheduler")
    public AbstractScheduledService.Scheduler bitcoinRegtestBlockMiningScheduler() {
        return new AbstractScheduledService.CustomScheduler() {
            private final Duration MIN_BLOCK_DURATION = Duration.ofSeconds(1);
            private final Duration MAX_BLOCK_DURATION = Duration.ofSeconds(10);

            @Override
            protected Schedule getNextSchedule() {
                long randomMillis = (long) Math.max(
                        MIN_BLOCK_DURATION.toMillis(),
                        MAX_BLOCK_DURATION.toMillis() * Math.random()
                );

                Duration durationTillNewBlock = Duration.ofMillis(randomMillis);

                log.debug("Duration till next block: {}", durationTillNewBlock);

                return new Schedule(durationTillNewBlock.toSeconds(), TimeUnit.SECONDS);
            }
        };
    }
}
