package org.tbk.spring.testcontainer.bitcoind.regtest;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.msgilligan.bitcoinj.json.pojo.BlockChainInfo;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import com.msgilligan.bitcoinj.rpc.RpcConfig;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.tbk.bitcoin.jsonrpc.config.BitcoinJsonRpcClientAutoConfigProperties;
import org.tbk.spring.testcontainer.bitcoind.BitcoindContainer;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class ScheduledBitcoindRegtestMinerTest {

    @SpringBootApplication
    public static class BitcoinContainerClientTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(BitcoinContainerClientTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }

        /**
         * Overwrite the default port of the rpc config as the mapping to the container
         * can only be determined during runtime.
         */
        @Bean
        public RpcConfig bitcoinJsonRpcConfig(NetworkParameters bitcoinNetworkParameters,
                                              BitcoinJsonRpcClientAutoConfigProperties properties,
                                              BitcoindContainer<?> bitcoinContainer) {
            URI uri = URI.create(properties.getRpchost() + ":" + bitcoinContainer.getMappedPort(properties.getRpcport()));
            return new RpcConfig(bitcoinNetworkParameters, uri, properties.getRpcuser(), properties.getRpcpassword());
        }


        @Bean(initMethod = "startAsync", destroyMethod = "stopAsync")
        public ScheduledBitcoindRegtestMiner scheduledBitcoindRegtestMiner(BitcoinClient bitcoinJsonRpcClient,
                                                                           @Qualifier("bitcoindRegtestMinerScheduler")
                                                                                   AbstractScheduledService.Scheduler scheduler) {
            return new ScheduledBitcoindRegtestMiner(bitcoinJsonRpcClient, scheduler);
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
    public void testGetBlockChainInfo() throws IOException {
        BlockChainInfo initBlockChainInfo = bitcoinJsonRpcClient.getBlockChainInfo();
        assertThat(initBlockChainInfo.getChain(), is("regtest"));

        int initBlocks = initBlockChainInfo.getBlocks();

        Sha256Hash initBestBlockHash = initBlockChainInfo.getBestBlockHash();

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
        assertThat(nextBestBlockHash, is(not(initBestBlockHash)));

        BlockChainInfo currentBlockChainInfo = bitcoinJsonRpcClient.getBlockChainInfo();
        assertThat(currentBlockChainInfo.getBlocks(), is(greaterThan(initBlocks)));
    }
}
