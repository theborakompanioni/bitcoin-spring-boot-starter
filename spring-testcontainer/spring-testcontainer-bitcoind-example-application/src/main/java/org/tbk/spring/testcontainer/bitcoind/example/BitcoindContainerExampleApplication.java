package org.tbk.spring.testcontainer.bitcoind.example;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.consensusj.bitcoin.json.pojo.BlockChainInfo;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;

import static java.util.Objects.requireNonNull;

@Slf4j
@SpringBootApplication
public class BitcoindContainerExampleApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(BitcoindContainerExampleApplication.class)
                .listeners(applicationPidFileWriter())
                .web(WebApplicationType.NONE)
                .profiles("development", "local")
                .run(args);
    }

    public static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }


    private final BitcoinClient bitcoinJsonRpcClient;

    public BitcoindContainerExampleApplication(BitcoinClient bitcoinJsonRpcClient) {
        this.bitcoinJsonRpcClient = requireNonNull(bitcoinJsonRpcClient);
    }

    @Bean
    @Profile("!test")
    public ApplicationRunner mainRunner(MessagePublishService<Block> bitcoinBlockPublishService) {
        return args -> {
            bitcoinBlockPublishService.awaitRunning(Duration.ofSeconds(20));
            log.info("=================================================");
            Flux.from(bitcoinBlockPublishService).subscribe(val -> {
                try {
                    BlockChainInfo blockChainInfo = bitcoinJsonRpcClient.getBlockChainInfo();
                    log.info("=================================================");
                    log.info("bestblock: {}", blockChainInfo.getBestBlockHash());
                    log.info("height: {}", blockChainInfo.getBlocks());
                    log.info("chain: {}", blockChainInfo.getChain());
                } catch (IOException e) {
                    log.error("", e);
                }
            });
        };
    }
}
