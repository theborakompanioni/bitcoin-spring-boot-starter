package org.tbk.spring.testcontainer.electrumd.example;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.consensusj.bitcoin.json.pojo.BlockChainInfo;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.ValidationException;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.command.GetInfoResponse;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Configuration(proxyBeanMethods = false)
class ElectrumDaemonExampleApplicationConfig {

    @Bean
    @Profile("!test")
    ApplicationRunner electrumDaemonStatusLogger(MessagePublishService<Block> bitcoinBlockPublishService,
                                                 ElectrumClient electrumClient) {
        return args -> {
            bitcoinBlockPublishService.awaitRunning(Duration.ofSeconds(20));

            Disposable subscription = Flux.from(bitcoinBlockPublishService).subscribe(val -> {
                try {
                    GetInfoResponse daemonStatus = electrumClient.getInfo();
                    log.info("[electrum] blockchain height: {} (server height: {})", daemonStatus.getBlockchainHeight(), daemonStatus.getServerHeight());
                } catch (Exception e) {
                    log.error("", e);
                }
            });

            Runtime.getRuntime().addShutdownHook(new Thread(subscription::dispose));
        };
    }

    @Bean
    @Profile("!test")
    ApplicationRunner bestBlockLogger(BitcoinClient bitcoinJsonRpcClient,
                                      MessagePublishService<Block> bitcoinBlockPublishService) {
        return args -> {
            bitcoinBlockPublishService.awaitRunning(Duration.ofSeconds(20));

            Disposable subscription = Flux.from(bitcoinBlockPublishService).subscribe(val -> {
                try {
                    BlockChainInfo info = bitcoinJsonRpcClient.getBlockChainInfo();
                    log.info("[bitcoind] new best block (height: {}): {}", info.getBlocks(), info.getBestBlockHash());
                } catch (IOException e) {
                    log.error("", e);
                }
            });

            Runtime.getRuntime().addShutdownHook(new Thread(subscription::dispose));
        };
    }

    @Bean
    @Profile("!test")
    ApplicationRunner lndBestBlockLogger(MessagePublishService<Block> bitcoinBlockPublishService,
                                         SynchronousLndAPI lndApi) {
        return args -> {
            bitcoinBlockPublishService.awaitRunning(Duration.ofSeconds(20));

            Disposable subscription = Flux.from(bitcoinBlockPublishService).subscribe(val -> {
                try {
                    org.lightningj.lnd.wrapper.message.GetInfoResponse info = lndApi.getInfo();
                    log.info("[lnd] block hash (height: {}): {}", info.getBlockHeight(), info.getBlockHash());
                } catch (StatusException | ValidationException e) {
                    log.error("", e);
                }
            });

            Runtime.getRuntime().addShutdownHook(new Thread(subscription::dispose));
        };
    }
}
