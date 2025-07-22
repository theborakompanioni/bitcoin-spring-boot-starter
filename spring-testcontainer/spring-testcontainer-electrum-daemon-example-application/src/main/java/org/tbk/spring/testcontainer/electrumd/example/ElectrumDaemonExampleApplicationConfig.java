package org.tbk.spring.testcontainer.electrumd.example;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.consensusj.bitcoin.json.pojo.BlockChainInfo;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.rpc.command.GetInfoResponse;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Configuration(proxyBeanMethods = false)
class ElectrumDaemonExampleApplicationConfig {

    @Bean
    @Profile("!test")
    CommandLineRunner keepApplicationAlive() {
        return args -> {
            CountDownLatch closeLatch = new CountDownLatch(1);
            Runtime.getRuntime().addShutdownHook(new Thread(closeLatch::countDown));
            closeLatch.await();
        };
    }

    @Bean
    @Profile("!test")
    ApplicationRunner electrumDaemonStatusLoggerPeriodic(ElectrumClient electrumClient) {
        return args -> {
            Disposable subscription = Flux.interval(Duration.ofSeconds(1), Duration.ofSeconds(10))
                    .subscribeOn(Schedulers.single())
                    .subscribe(val -> {
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
    ApplicationRunner electrumDaemonStatusLoggerOnBlock(ElectrumClient electrumClient,
                                                        @Autowired(required = false) MessagePublishService<Block> bitcoinBlockPublishService) {
        if (bitcoinBlockPublishService == null) {
            return args -> {
            };
        }
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
    ApplicationRunner bestBlockLogger(@Autowired(required = false) BitcoinClient bitcoinJsonRpcClient,
                                      @Autowired(required = false) MessagePublishService<Block> bitcoinBlockPublishService) {
        if (bitcoinJsonRpcClient == null || bitcoinBlockPublishService == null) {
            return args -> {
            };
        }

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
}
