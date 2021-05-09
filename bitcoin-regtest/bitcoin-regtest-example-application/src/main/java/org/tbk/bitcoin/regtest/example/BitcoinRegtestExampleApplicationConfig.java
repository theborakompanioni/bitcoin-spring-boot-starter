package org.tbk.bitcoin.regtest.example;

import com.google.common.base.Stopwatch;
import com.msgilligan.bitcoinj.json.pojo.BlockChainInfo;
import com.msgilligan.bitcoinj.json.pojo.NetworkInfo;
import com.msgilligan.bitcoinj.json.pojo.TxOutSetInfo;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Coin;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.command.DaemonLoadWalletRequest;
import org.tbk.electrum.command.DaemonStatusResponse;
import org.tbk.electrum.model.Balance;
import org.tbk.electrum.model.History;
import org.tbk.electrum.model.TxoValue;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Configuration
@Profile("!test")
public class BitcoinRegtestExampleApplicationConfig {

    private static String friendlyBtcString(TxoValue txoValue) {
        return Coin.valueOf(txoValue.getValue()).toFriendlyString();
    }

    @Bean
    public CommandLineRunner logZmqRawBlocksMessages(MessagePublishService<Block> bitcoinjBlockPublishService) {
        return args -> {
            AtomicLong zeromqBlockCounter = new AtomicLong();
            Flux.from(bitcoinjBlockPublishService).subscribe(arg -> {
                log.info("Received zeromq message: {} - {}", zeromqBlockCounter.incrementAndGet(), arg.getHash());
            });

            bitcoinjBlockPublishService.awaitRunning(Duration.ofSeconds(10));
        };
    }

    @Bean
    public CommandLineRunner logBitcoinStatus(MessagePublishService<Block> bitcoinjBlockPublishService,
                                              BitcoinClient bitcoinClient) {
        return args -> {
            Flux.from(bitcoinjBlockPublishService).subscribe(arg -> {
                try {
                    TxOutSetInfo txOutSetInfo = bitcoinClient.getTxOutSetInfo();
                    NetworkInfo networkInfo = bitcoinClient.getNetworkInfo();
                    BlockChainInfo blockChainInfo = bitcoinClient.getBlockChainInfo();
                    log.info("============================");
                    log.info("Bitcoin Core ({}) Status", networkInfo.getSubVersion());
                    log.info("Chain: {}", blockChainInfo.getChain());
                    log.info("Connections: {}", networkInfo.getConnections());
                    log.info("Headers: {}", blockChainInfo.getHeaders());
                    log.info("Blocks: {}", blockChainInfo.getBlocks());
                    log.info("Best block hash: {}", blockChainInfo.getBestBlockHash());
                    log.info("Difficulty: {}", blockChainInfo.getDifficulty().toPlainString());
                    log.info("UTXO: {} ({})", txOutSetInfo.getTransactions(), txOutSetInfo.getTotalAmount().toFriendlyString());
                    log.info("============================");
                } catch (IOException e) {
                    log.error("", e);
                }
            });

            bitcoinjBlockPublishService.awaitRunning(Duration.ofSeconds(10));
        };
    }

    @Bean
    public CommandLineRunner logElectrumStatus(MessagePublishService<Block> bitcoinjBlockPublishService,
                                               ElectrumClient electrumClient) {
        return args -> {
            Flux.from(bitcoinjBlockPublishService).subscribe(arg -> {
                try {
                    DaemonStatusResponse daemonStatusResponse = electrumClient.daemonStatus();
                    Boolean walletSynchronized = electrumClient.isWalletSynchronized();

                    log.info("============================");
                    log.info("Electrum Daemon ({}) Status", daemonStatusResponse.getVersion());
                    log.info("Connected: {}", daemonStatusResponse.isConnected());
                    log.info("Blockheight: {}/{}", daemonStatusResponse.getBlockchainHeight(), daemonStatusResponse.getServerHeight());
                    log.info("Current wallet: {}", daemonStatusResponse.getCurrentWallet().orElse("<none>"));
                    log.info("Wallet synchronized: {}", walletSynchronized);
                    if (Boolean.TRUE == walletSynchronized) {
                        History history = electrumClient.getHistory();

                        log.info("Transactions: {}", history.getTransactions().size());

                        Balance balance = electrumClient.getBalance();

                        log.info("Balance: {} total", friendlyBtcString(balance.getTotal()));
                        log.info("         {} confirmed", friendlyBtcString(balance.getConfirmed()));
                        log.info("         {} unconfirmed", friendlyBtcString(balance.getUnconfirmed()));
                        log.info("         {} spendable", friendlyBtcString(balance.getSpendable()));
                        log.info("         {} unmatured", friendlyBtcString(balance.getUnmatured()));
                    }
                    log.info("============================");
                } catch (Exception e) {
                    log.error("", e);
                }
            });

            bitcoinjBlockPublishService.awaitRunning(Duration.ofSeconds(10));
        };
    }

    @Bean
    public CommandLineRunner loadElectrumWallet(ElectrumClient electrumClient) {
        return args -> {
            boolean daemonConnected = electrumClient.isDaemonConnected();
            log.info("electrum daemon connected: {}", daemonConnected);

            Boolean loadWalletResult = electrumClient.loadWallet(DaemonLoadWalletRequest.builder()
                    .walletPath("/home/electrum/.electrum/regtest/wallets/default_wallet")
                    .build());
            log.info("electrum load wallet result: {}", loadWalletResult);

            boolean walletSynchronized = electrumClient.isWalletSynchronized();
            log.info("electrum wallet synchronized: {}", walletSynchronized);

            if (!walletSynchronized) {
                log.info("Will wait max. 30s for electrum wallet to synchronize..");
                Stopwatch sw = Stopwatch.createStarted();
                Boolean walletSynchronizedAfterWaiting = Flux.interval(Duration.ofMillis(100))
                        .map(it -> electrumClient.isWalletSynchronized())
                        .filter(it -> it)
                        .blockFirst(Duration.ofSeconds(30));

                log.info("Electrum wallet synchronized after {}: {}", sw.stop(), walletSynchronizedAfterWaiting);

                if (walletSynchronizedAfterWaiting != Boolean.TRUE) {
                    throw new IllegalStateException("Could not synchronized electrum wallet");
                }
            }
        };
    }
}
