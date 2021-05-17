package org.tbk.bitcoin.regtest.scenario;

import com.google.common.base.Stopwatch;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Sha256Hash;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.bitcoin.regtest.mining.RegtestMiner;
import org.tbk.bitcoin.regtest.mining.RegtestMinerImpl;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
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
        public RegtestMiner regtestMiner(BitcoinClient bitcoinJsonRpcClient) {
            return new RegtestMinerImpl(bitcoinJsonRpcClient);
        }

        @Bean
        public BitcoinRegtestActions bitcoinScenarioFactory(RegtestMiner regtestMiner) {
            return new BitcoinRegtestActions(regtestMiner);
        }
    }

    @Autowired
    private BitcoinRegtestActions bitcoinScenarioFactory;

    @Test
    void itShouldHaveFluentSyntaxToMineBlocks() {
        Stopwatch sw = Stopwatch.createStarted();

        List<Sha256Hash> blockHashes = Mono.from(bitcoinScenarioFactory.mineBlock())
                .concatWith(bitcoinScenarioFactory.mineBlock())
                .concatWith(bitcoinScenarioFactory.mineBlock())
                .flatMapIterable(it -> it)
                .collectList()
                .block();

        log.debug("Finished after {}", sw.stop());

        assertThat(blockHashes, hasSize(3));
    }

}
