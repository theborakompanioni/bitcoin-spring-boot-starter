package org.tbk.electrum.gateway.example;

import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;
import com.msgilligan.bitcoinj.json.pojo.BlockChainInfo;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.gateway.example.watch.ElectrumDaemonWalletSendBalance;
import org.tbk.electrum.gateway.example.watch.ElectrumWalletWatchLoop;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@EnableConfigurationProperties(ElectrumGatewayExampleApplicationProperties.class)
public class ElectrumGatewayExampleApplicationConfig {

    private final ElectrumGatewayExampleApplicationProperties properties;

    public ElectrumGatewayExampleApplicationConfig(ElectrumGatewayExampleApplicationProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean(initMethod = "startAsync", destroyMethod = "stopAsync")
    public ElectrumWalletWatchLoop electrumWalletWatchLoop(ElectrumClient electrumClient) {
        Scheduler scheduler = Scheduler.newFixedDelaySchedule(
                this.properties.getInitialDelay(),
                this.properties.getDelay(),
                this.properties.getTimeUnitOrThrow()
        );

        String destinationAddress = this.properties.getDestinationAddress();

        ElectrumDaemonWalletSendBalance.Options build = ElectrumDaemonWalletSendBalance.Options.builder()
                .walletPassphrase(null)
                .destinationAddress(destinationAddress)
                .build();

        return new ElectrumWalletWatchLoop(electrumClient, build, scheduler);
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
