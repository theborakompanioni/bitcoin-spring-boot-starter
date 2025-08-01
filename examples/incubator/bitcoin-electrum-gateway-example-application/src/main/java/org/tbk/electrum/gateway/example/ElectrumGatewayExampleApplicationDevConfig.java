package org.tbk.electrum.gateway.example;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Block;
import org.consensusj.bitcoin.jsonrpc.BitcoinClient;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.common.WalletParams;

import static org.quartz.SimpleScheduleBuilder.repeatSecondlyForever;
import static org.tbk.bitcoin.regtest.common.BitcoindStatusLogging.logBitcoinStatusOnNewBlock;
import static org.tbk.bitcoin.regtest.electrum.common.ElectrumdStatusLogging.logElectrumStatusOnNewBlock;

@Slf4j
@Configuration(proxyBeanMethods = false)
@Profile("development")
class ElectrumGatewayExampleApplicationDevConfig {

    @Bean
    Trigger fundWalletJobTrigger(JobDetail fundWalletJobDetail) {
        return TriggerBuilder.newTrigger().forJob(fundWalletJobDetail)
                .withIdentity("FundWalletJob")
                .withDescription("Trigger FundWalletJob every 21s")
                .withSchedule(repeatSecondlyForever(21))
                .startNow()
                .build();
    }

    @Bean
    @Profile("!test")
    CommandLineRunner logBitcoinStatus(MessagePublishService<Block> bitcoinjBlockPublishService,
                                       BitcoinClient bitcoinClient) {
        return args -> logBitcoinStatusOnNewBlock(bitcoinjBlockPublishService, bitcoinClient);
    }

    @Bean
    @Profile("!test")
    CommandLineRunner logElectrumStatus(MessagePublishService<Block> bitcoinjBlockPublishService,
                                        ElectrumClient electrumClient,
                                        WalletParams walletParams) {
        return args -> logElectrumStatusOnNewBlock(bitcoinjBlockPublishService, electrumClient, walletParams);
    }
}
