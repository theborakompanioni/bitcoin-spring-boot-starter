package org.tbk.bitcoin.regtest.scenario;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Sha256Hash;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.bitcoin.regtest.mining.RegtestMiner;
import org.tbk.bitcoin.regtest.mining.RegtestMinerImpl;
import org.tbk.spring.testcontainer.bitcoind.BitcoindContainer;
import org.tbk.spring.testcontainer.test.MoreTestcontainerTestUtil;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BitcoinRegtestScenarioTest {

    @SpringBootApplication(proxyBeanMethods = false)
    public static class BitcoinContainerClientTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(BitcoinContainerClientTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }

        @Bean
        RegtestMiner regtestMiner(BitcoinClient bitcoinJsonRpcClient) {
            return new RegtestMinerImpl(bitcoinJsonRpcClient);
        }

        @Bean
        BitcoinRegtestActions bitcoinRegtestActions(RegtestMiner regtestMiner) {
            return new BitcoinRegtestActions(regtestMiner);
        }
    }

    @Autowired
    private BitcoindContainer<?> bitcoindContainer;

    @Autowired
    private BitcoinRegtestActions bitcoinRegtestActions;

    @Test
    @Order(1)
    void contextLoads() {
        assertThat(bitcoinRegtestActions, is(notNullValue()));

        assertThat(bitcoindContainer, is(notNullValue()));
        assertThat("bitcoind daemon container is running", bitcoindContainer.isRunning(), is(true));

        Boolean ranForMinimumDuration = MoreTestcontainerTestUtil.ranForMinimumDuration(bitcoindContainer).block();
        assertThat("container ran for the minimum amount of time to be considered healthy", ranForMinimumDuration, is(true));
    }

    @Test
    void itShouldHaveFluentSyntaxToMineBlocks() {
        Stopwatch sw = Stopwatch.createStarted();

        List<Sha256Hash> blockHashes = Mono.from(bitcoinRegtestActions.mineBlock())
                .concatWith(bitcoinRegtestActions.mineBlock())
                .concatWith(bitcoinRegtestActions.mineBlock())
                .flatMapIterable(it -> it)
                .collectList()
                .block();

        log.debug("Finished after {}", sw.stop());

        assertThat(blockHashes, hasSize(3));
    }

}
