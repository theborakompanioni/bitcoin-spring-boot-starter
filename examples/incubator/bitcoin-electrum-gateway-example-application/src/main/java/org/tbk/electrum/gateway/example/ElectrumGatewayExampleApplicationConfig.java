package org.tbk.electrum.gateway.example;

import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.gateway.example.watch.ElectrumDaemonWalletSendBalance;
import org.tbk.electrum.gateway.example.watch.ElectrumWalletWatchLoop;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static org.tbk.bitcoin.regtest.common.BitcoindStatusLogging.logBitcoinStatusOnNewBlock;
import static org.tbk.bitcoin.regtest.electrum.common.ElectrumdStatusLogging.logElectrumStatusOnNewBlock;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ElectrumGatewayExampleApplicationProperties.class)
public class ElectrumGatewayExampleApplicationConfig {

    private final ElectrumGatewayExampleApplicationProperties properties;

    public ElectrumGatewayExampleApplicationConfig(ElectrumGatewayExampleApplicationProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean(destroyMethod = "stopAsync")
    public ElectrumWalletWatchLoop electrumWalletWatchLoop(ElectrumClient electrumClient) {
        Scheduler scheduler = Scheduler.newFixedDelaySchedule(
                this.properties.getInitialDelay().toNanos(),
                this.properties.getDelay().toNanos(),
                TimeUnit.NANOSECONDS
        );

        String destinationAddress = this.properties.getDestinationAddress();

        ElectrumDaemonWalletSendBalance.Options build = ElectrumDaemonWalletSendBalance.Options.builder()
                .walletPassphrase(null)
                .destinationAddress(destinationAddress)
                .build();

        ElectrumWalletWatchLoop electrumWalletWatchLoop = new ElectrumWalletWatchLoop(electrumClient, build, scheduler);
        return (ElectrumWalletWatchLoop) electrumWalletWatchLoop.startAsync();
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner logBitcoinStatus(MessagePublishService<Block> bitcoinjBlockPublishService,
                                              BitcoinClient bitcoinClient) {
        return args -> logBitcoinStatusOnNewBlock(bitcoinjBlockPublishService, bitcoinClient);
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner logElectrumStatus(MessagePublishService<Block> bitcoinjBlockPublishService,
                                               ElectrumClient electrumClient) {
        return args -> logElectrumStatusOnNewBlock(bitcoinjBlockPublishService, electrumClient);
    }
}
