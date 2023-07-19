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
import org.lightningj.lnd.wrapper.message.Chain;
import org.lightningj.lnd.wrapper.message.GetInfoResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.tbk.bitcoin.regtest.BitcoindRegtestTestHelper;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.lightning.cln.grpc.client.GetinfoRequest;
import org.tbk.lightning.cln.grpc.client.GetinfoResponse;
import org.tbk.lightning.cln.grpc.client.NodeGrpc;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
@Configuration(proxyBeanMethods = false)
class LnPlaygroundExampleApplicationConfig {

    /**
     * We must have access to a wallet for "getnewaddress" command to work.
     * Create a wallet if none is found (currently only when in regtest mode)!
     * Maybe move to {@link org.tbk.bitcoin.regtest.config.BitcoinRegtestAutoConfiguration}?
     */
    @Bean
    @Profile("!test")
    InitializingBean createWalletIfMissing(BitcoinClient bitcoinClient) {
        return () -> BitcoindRegtestTestHelper.createDefaultWalletIfNecessary(bitcoinClient);
    }

    @Bean
    @Profile("!test")
    ApplicationRunner clnPrintInfoRunner(NodeGrpc.NodeFutureStub clnNodeFutureStub) {
        return args -> {
            GetinfoResponse info = clnNodeFutureStub.getinfo(GetinfoRequest.newBuilder().build())
                    .get(10, TimeUnit.SECONDS);
            log.info("=================================================");
            log.info("[cln] id: {}", HexFormat.of().formatHex(info.getId().toByteArray()));
            log.info("[cln] alias: {}", info.getAlias());
            log.info("[cln] version: {}", info.getVersion());
            log.info("[cln] network: {}", info.getNetwork());
        };
    }

    @Bean
    @Profile("!test")
    ApplicationRunner lndPrintInfoRunner(SynchronousLndAPI lndApi,
                                         SynchronousAutopilotAPI autopilotApi) {
        return args -> {
            GetInfoResponse info = lndApi.getInfo();
            StatusResponse autopilotStatus = autopilotApi.status();
            log.info("=================================================");
            log.info("[lnd] id: {}", info.getIdentityPubkey());
            log.info("[lnd] alias: {}", info.getAlias());
            log.info("[lnd] version: {}", info.getVersion());
            log.info("[lnd] network: {}", info.getChains().stream()
                    .map(Chain::getNetwork)
                    .collect(Collectors.joining(",")));
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
    ApplicationRunner clnBestBlockLogger(MessagePublishService<Block> bitcoinBlockPublishService,
                                         NodeGrpc.NodeFutureStub clnNodeFutureStub) {
        return args -> {
            bitcoinBlockPublishService.awaitRunning(Duration.ofSeconds(20));
            Disposable subscription = Flux.from(bitcoinBlockPublishService).subscribe(val -> {
                try {
                    GetinfoResponse info = clnNodeFutureStub.getinfo(GetinfoRequest.newBuilder().build())
                            .get(10, TimeUnit.SECONDS);
                    log.info("[cln] block height: {}", info.getBlockheight());
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
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
                    GetInfoResponse info = lndApi.getInfo();
                    log.info("[lnd] block height: {}", info.getBlockHeight());
                } catch (StatusException | ValidationException e) {
                    log.error("", e);
                }
            });

            Runtime.getRuntime().addShutdownHook(new Thread(subscription::dispose));
        };
    }
}
