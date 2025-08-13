package org.tbk.bitcoin.regtest.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.tbk.bitcoin.regtest.electrum.faucet.ElectrumRegtestFaucet;
import org.tbk.bitcoin.regtest.electrum.faucet.SimpleElectrumRegtestFaucet;
import org.tbk.bitcoin.regtest.electrum.scenario.ElectrumRegtestActions;
import org.tbk.bitcoin.regtest.scenario.BitcoinRegtestActions;
import org.tbk.electrum.bitcoinj.BitcoinjElectrumClient;
import org.tbk.electrum.bitcoinj.config.BitcoinjElectrumJsonrpcClientAutoConfiguration;
import org.tbk.electrum.common.WalletParams;

import java.time.Instant;
import java.util.UUID;

@AutoConfiguration
@ConditionalOnClass({
        BitcoinjElectrumClient.class,
        BitcoinRegtestActions.class,
        ElectrumRegtestFaucet.class
})
@AutoConfigureAfter({
        BitcoinjElectrumJsonrpcClientAutoConfiguration.class,
        BitcoinRegtestActionsAutoConfiguration.class,
})
@ConditionalOnProperty(value = "org.tbk.bitcoin.regtest.electrum.enabled", havingValue = "true", matchIfMissing = true)
public class ElectrumRegtestAutoConfiguration {

    @Bean
    @ConditionalOnBean(BitcoinjElectrumClient.class)
    @ConditionalOnMissingBean
    ElectrumRegtestActions electrumRegtestActions(BitcoinjElectrumClient electrumClient) {
        return new ElectrumRegtestActions(electrumClient);
    }

    @Bean
    @ConditionalOnBean({
            BitcoinjElectrumClient.class,
            BitcoinRegtestActions.class
    })
    @ConditionalOnMissingBean
    ElectrumRegtestFaucet electrumRegtestFaucet(BitcoinjElectrumClient electrumClient,
                                                BitcoinRegtestActions bitcoinRegtestActions) {
        return new SimpleElectrumRegtestFaucet(
                electrumClient,
                bitcoinRegtestActions,
                faucetWalletParams()
        );
    }

    @SuppressFBWarnings(value = "HARD_CODE_PASSWORD", justification = "okay for a regtest faucet")
    WalletParams faucetWalletParams() {
        String pseudoRandomPostfix = UUID.randomUUID().toString().substring(0, 8);
        String walletName = "faucet_%d_%s".formatted(Instant.now().toEpochMilli(), pseudoRandomPostfix);
        return WalletParams.builder()
                .walletPath(walletName)
                .password("faucet")
                .build();
    }
}
