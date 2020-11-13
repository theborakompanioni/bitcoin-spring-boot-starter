package org.tbk.spring.bitcoin.testcontainer.example;

import com.msgilligan.bitcoinj.json.pojo.BlockChainInfo;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

@Slf4j
@SpringBootApplication
public class BitcoinContainerExampleApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(BitcoinContainerExampleApplication.class)
                .listeners(applicationPidFileWriter())
                .web(WebApplicationType.NONE)
                .profiles("development", "local")
                .run(args);
    }

    public static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }


    private final BitcoinClient bitcoinJsonRpcClient;

    public BitcoinContainerExampleApplication(BitcoinClient bitcoinJsonRpcClient) {
        this.bitcoinJsonRpcClient = requireNonNull(bitcoinJsonRpcClient);
    }

    @Scheduled(fixedDelay = 1000)
    public void printBlockHeightRunner() {
        try {
            BlockChainInfo blockChainInfo = bitcoinJsonRpcClient.getBlockChainInfo();
            log.info("=================================================");
            log.info("bestblock: {}", blockChainInfo.getBestBlockHash());
            log.info("height: {}", blockChainInfo.getBlocks());
            log.info("chain: {}", blockChainInfo.getChain());
        } catch (IOException e) {
            log.error("", e);
        }
    }
}
