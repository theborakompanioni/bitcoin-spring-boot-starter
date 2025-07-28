package org.tbk.bitcoin.regtest.example;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.common.WalletParams;
import org.tbk.electrum.rpc.command.IsSynchronizedParams;
import org.tbk.electrum.rpc.command.LoadWalletParams;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.tbk.bitcoin.regtest.common.BitcoindStatusLogging.logBitcoinStatusOnNewBlock;
import static org.tbk.bitcoin.regtest.electrum.common.ElectrumdStatusLogging.logElectrumStatusOnNewBlock;

@Slf4j
@Configuration(proxyBeanMethods = false)
@Profile("!test")
class BitcoinRegtestExampleApplicationConfig {

    @Bean
    WalletParams defaultWalletParams() {
        return WalletParams.builder()
                .walletPath("/home/electrum/.electrum/regtest/wallets/default_wallet")
                .password(null)
                .build();
    }

    @Bean
    CommandLineRunner logZmqRawBlocksMessages(MessagePublishService<Block> bitcoinjBlockPublishService) {
        return args -> {
            AtomicLong zeromqBlockCounter = new AtomicLong();
            Disposable subscription = Flux.from(bitcoinjBlockPublishService).subscribe(arg -> {
                log.info("Received zeromq message: {} - {}", zeromqBlockCounter.incrementAndGet(), arg.getHash());
            });
            Runtime.getRuntime().addShutdownHook(new Thread(subscription::dispose));

            bitcoinjBlockPublishService.awaitRunning(Duration.ofSeconds(10));
        };
    }

    @Bean
    CommandLineRunner logBitcoinStatus(MessagePublishService<Block> bitcoinjBlockPublishService,
                                       BitcoinClient bitcoinClient) {
        return args -> logBitcoinStatusOnNewBlock(bitcoinjBlockPublishService, bitcoinClient);
    }

    @Bean
    CommandLineRunner logElectrumStatus(MessagePublishService<Block> bitcoinjBlockPublishService,
                                        ElectrumClient electrumClient,
                                        WalletParams walletParams) {
        return args -> logElectrumStatusOnNewBlock(bitcoinjBlockPublishService, electrumClient, walletParams);
    }

    @Bean
    CommandLineRunner loadElectrumWallet(ElectrumClient electrumClient, WalletParams walletParams) {
        return args -> {
            boolean daemonConnected = electrumClient.isConnected();
            log.info("electrum daemon connected: {}", daemonConnected);

            Boolean loadWalletResult = electrumClient.loadWallet(LoadWalletParams.builder()
                    .walletPath(walletParams.getWalletPath())
                    .password(walletParams.getPassword().orElse(null))
                    .build());
            log.info("electrum load wallet result: {}", loadWalletResult);

            IsSynchronizedParams synchronizedParams = IsSynchronizedParams.builder()
                    .walletPath(walletParams.getWalletPath())
                    .build();
            boolean walletSynchronized = electrumClient.isWalletSynchronized(synchronizedParams);
            log.info("electrum wallet synchronized: {}", walletSynchronized);

            if (!walletSynchronized) {
                Duration timeout = Duration.ofSeconds(60);
                log.info("Will wait max. {} for electrum wallet to synchronize..", timeout);
                Stopwatch sw = Stopwatch.createStarted();
                Boolean walletSynchronizedAfterWaiting = Flux.interval(Duration.ofMillis(100))
                        .map(it -> electrumClient.isWalletSynchronized(synchronizedParams))
                        .filter(it -> it)
                        .blockFirst(timeout);

                log.info("Electrum wallet synchronized after {}: {}", sw.stop(), walletSynchronizedAfterWaiting);

                if (!Boolean.TRUE.equals(walletSynchronizedAfterWaiting)) {
                    throw new IllegalStateException("Could not synchronized electrum wallet");
                }
            }
        };
    }
}
