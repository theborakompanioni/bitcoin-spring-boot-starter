package org.tbk.bitcoin.regtest;

import com.google.common.util.concurrent.AbstractScheduledService;
import org.consensusj.bitcoin.json.pojo.BlockChainInfo;
import org.consensusj.bitcoin.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Sha256Hash;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.bitcoin.regtest.mining.RegtestMiner;
import org.tbk.bitcoin.regtest.mining.RegtestMinerImpl;
import org.tbk.bitcoin.regtest.mining.ScheduledRegtestMiner;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class ScheduledRegtestMinerTest {

    @SpringBootApplication(proxyBeanMethods = false)
    public static class BitcoinContainerClientTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(BitcoinContainerClientTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }

        @Bean
        public RegtestMiner regtestMiner(BitcoinClient bitcoinJsonRpcClient) {
            return new RegtestMinerImpl(bitcoinJsonRpcClient);
        }

        @Bean(destroyMethod = "stopAsync")
        public ScheduledRegtestMiner scheduledregtestMiner(RegtestMiner regtestMiner,
                                                           @Qualifier("regtestMinerScheduler")
                                                                   AbstractScheduledService.Scheduler scheduler) {
            ScheduledRegtestMiner scheduledregtestMiner = new ScheduledRegtestMiner(regtestMiner, scheduler);
            scheduledregtestMiner.startAsync();
            return scheduledregtestMiner;
        }

        @Bean("regtestMinerScheduler")
        public AbstractScheduledService.Scheduler regtestMinerScheduler() {
            return new RegtestMinerScheduler();
        }

        private static class RegtestMinerScheduler extends AbstractScheduledService.CustomScheduler {
            private static final Duration MIN_BLOCK_DURATION = Duration.ofMillis(1000);
            private static final Duration MAX_BLOCK_DURATION = Duration.ofMillis(3000);
            
            private final SecureRandom random = new SecureRandom();

            @Override
            protected Schedule getNextSchedule() {
                long randomMillis = (long) Math.max(
                        MIN_BLOCK_DURATION.toMillis(),
                        MAX_BLOCK_DURATION.toMillis() * random.nextDouble()
                );

                Duration durationTillNewBlock = Duration.ofMillis(randomMillis);

                log.debug("Duration till next block: {}", durationTillNewBlock);

                return new Schedule(durationTillNewBlock.toSeconds(), TimeUnit.SECONDS);
            }
        }
    }

    @Autowired
    private BitcoinClient bitcoinJsonRpcClient;

    @Test
    void itShouldVerifyNewBestBlockHashChangesWhenNewBlockIsFound() throws IOException {
        BlockChainInfo initBlockChainInfo = bitcoinJsonRpcClient.getBlockChainInfo();
        assertThat(initBlockChainInfo.getChain(), is("regtest"));

        int initBlocks = initBlockChainInfo.getBlocks();

        Sha256Hash initBestBlockHash = initBlockChainInfo.getBestBlockHash();

        // poll for a new block and returned the first arriving block - timeout if it takes too long
        Sha256Hash nextBestBlockHash = Flux.interval(Duration.ofMillis(100))
                .map(i -> {
                    try {
                        return bitcoinJsonRpcClient.getBlockChainInfo();
                    } catch (IOException e) {
                        throw new IllegalStateException("Error while fetching blockchain info", e);
                    }
                })
                .map(BlockChainInfo::getBestBlockHash)
                .filter(currentBestBlockHash -> !initBestBlockHash.equals(currentBestBlockHash))
                .blockFirst(Duration.ofSeconds(10));

        assertThat(nextBestBlockHash, is(notNullValue()));
        assertThat("best block hash has changed", nextBestBlockHash, is(not(initBestBlockHash)));

        BlockChainInfo currentBlockChainInfo = bitcoinJsonRpcClient.getBlockChainInfo();
        assertThat("a new block has been mined", currentBlockChainInfo.getBlocks(), is(greaterThan(initBlocks)));
    }
}
