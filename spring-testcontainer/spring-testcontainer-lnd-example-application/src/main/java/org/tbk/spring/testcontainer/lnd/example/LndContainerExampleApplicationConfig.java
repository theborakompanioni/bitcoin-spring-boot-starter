package org.tbk.spring.testcontainer.lnd.example;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.msgilligan.bitcoinj.json.pojo.BlockChainInfo;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.spring.testcontainer.lnd.example.regtest.ScheduledBitcoinContainerRegtestMiner;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class LndContainerExampleApplicationConfig {

    @Bean
    public ApplicationRunner bestBlockLogger(BitcoinClient bitcoinJsonRpcClient,
                                             MessagePublishService<Block> bitcoinBlockPublishService) {
        return args -> {
            bitcoinBlockPublishService.awaitRunning(Duration.ofSeconds(20));
            Disposable subscription = Flux.from(bitcoinBlockPublishService).subscribe(val -> {
                try {
                    BlockChainInfo blockChainInfo = bitcoinJsonRpcClient.getBlockChainInfo();
                    log.info("[bitcoind] new best block: {}", blockChainInfo.getBestBlockHash());
                } catch (IOException e) {
                    log.error("", e);
                }
            });

            Runtime.getRuntime().addShutdownHook(new Thread(subscription::dispose));
        };
    }

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
