package org.tbk.lightning.playground.example;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.consensusj.bitcoin.json.pojo.BlockChainInfo;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.ValidationException;
import org.lightningj.lnd.wrapper.autopilot.SynchronousAutopilotAPI;
import org.lightningj.lnd.wrapper.autopilot.message.StatusResponse;
import org.lightningj.lnd.wrapper.message.GetInfoResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.tbk.bitcoin.regtest.BitcoindRegtestTestHelper;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class LnPlaygroundExampleApplicationConfig {

    /**
     * We must have access to a wallet for "getnewaddress" command to work.
     * Create a wallet if none is found (currently only when in regtest mode)!
     * Maybe move to {@link org.tbk.bitcoin.regtest.config.BitcoinRegtestAutoConfiguration}?
     */
    @Bean
    @Profile("!test")
    public InitializingBean createWalletIfMissing(BitcoinClient bitcoinRegtestClient) {
        return () -> BitcoindRegtestTestHelper.createDefaultWalletIfNecessary(bitcoinRegtestClient);
    }

    @Bean
    @Profile("!test")
    public ApplicationRunner mainRunner(SynchronousLndAPI lndApi,
                                        SynchronousAutopilotAPI autopilotApi) {
        return args -> {
            GetInfoResponse info = lndApi.getInfo();
            StatusResponse autopilotStatus = autopilotApi.status();
            log.info("=================================================");
            log.info("[lnd] identity_pubkey: {}", info.getIdentityPubkey());
            log.info("[lnd] alias: {}", info.getAlias());
            log.info("[lnd] version: {}", info.getVersion());
            log.info("[lnd] autopilot active: {}", autopilotStatus.getActive());
        };
    }

    @Bean
    @Profile("!test")
    public ApplicationRunner lndBestBlockLogger(SynchronousLndAPI lndApi,

                                                MessagePublishService<Block> bitcoinBlockPublishService) {
        return args -> {
            bitcoinBlockPublishService.awaitRunning(Duration.ofSeconds(20));
            Disposable subscription = Flux.from(bitcoinBlockPublishService).subscribe(val -> {
                try {
                    GetInfoResponse info = lndApi.getInfo();
                    log.info("=================================================");
                    log.info("[lnd] block height: {}", info.getBlockHeight());
                    log.info("[lnd] block hash: {}", info.getBlockHash());
                    log.info("[lnd] best header timestamp: {}", info.getBestHeaderTimestamp());
                } catch (StatusException | ValidationException e) {
                    log.error("", e);
                }
            });

            Runtime.getRuntime().addShutdownHook(new Thread(subscription::dispose));
        };
    }

    @Bean
    @Profile("!test")
    public ApplicationRunner bestBlockLogger(BitcoinClient bitcoinJsonRpcClient,
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
}
