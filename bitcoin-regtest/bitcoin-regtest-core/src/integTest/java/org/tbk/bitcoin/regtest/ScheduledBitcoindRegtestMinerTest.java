package org.tbk.bitcoin.regtest;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.msgilligan.bitcoinj.json.pojo.BlockChainInfo;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
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
import org.tbk.bitcoin.regtest.mining.BitcoindRegtestMiner;
import org.tbk.bitcoin.regtest.mining.BitcoindRegtestMinerImpl;
import org.tbk.bitcoin.regtest.mining.ScheduledBitcoindRegtestMiner;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class ScheduledBitcoindRegtestMinerTest {

    @SpringBootApplication(proxyBeanMethods = false)
    public static class BitcoinContainerClientTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(BitcoinContainerClientTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }

        @Bean
        public BitcoindRegtestMiner bitcoindRegtestMiner(BitcoinClient bitcoinJsonRpcClient) {
            return new BitcoindRegtestMinerImpl(bitcoinJsonRpcClient);
        }

        @Bean(destroyMethod = "stopAsync")
        public ScheduledBitcoindRegtestMiner scheduledBitcoindRegtestMiner(BitcoindRegtestMiner bitcoindRegtestMiner,
                                                                           @Qualifier("bitcoindRegtestMinerScheduler")
                                                                                   AbstractScheduledService.Scheduler scheduler) {
            ScheduledBitcoindRegtestMiner scheduledBitcoindRegtestMiner = new ScheduledBitcoindRegtestMiner(bitcoindRegtestMiner, scheduler);
            scheduledBitcoindRegtestMiner.startAsync();
            return scheduledBitcoindRegtestMiner;
        }

        @Bean("bitcoindRegtestMinerScheduler")
        public AbstractScheduledService.Scheduler bitcoindRegtestMinerScheduler() {
            return new AbstractScheduledService.CustomScheduler() {
                private final Duration MIN_BLOCK_DURATION = Duration.ofMillis(1000);
                private final Duration MAX_BLOCK_DURATION = Duration.ofMillis(3000);

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

    @Autowired
    private BitcoinClient bitcoinJsonRpcClient;

    @Test
    public void itShouldVerifyNewBestBlockHashChangesWhenNewBlockIsFound() throws IOException {
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
                        String message = "Error while fetching blockchain info";
                        throw new RuntimeException(message, e);
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
