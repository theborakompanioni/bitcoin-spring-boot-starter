package org.tbk.electrum.gateway.example;

import com.msgilligan.bitcoinj.json.pojo.BlockChainInfo;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Configuration
public class ElectrumGatewayExampleApplicationConfig {

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
}
