package org.tbk.electrum.bitcoinj.config;

import org.bitcoinj.core.NetworkParameters;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.bitcoinj.BitcoinjElectrumClient;
import org.tbk.electrum.bitcoinj.BitcoinjElectrumClientImpl;
import org.tbk.electrum.config.ElectrumDaemonJsonrpcClientAutoConfiguration;

@AutoConfiguration
@ConditionalOnClass(BitcoinjElectrumClient.class)
@AutoConfigureAfter(ElectrumDaemonJsonrpcClientAutoConfiguration.class)
public class BitcoinjElectrumJsonrpcClientAutoConfiguration {

    @Bean
    @ConditionalOnBean({
            NetworkParameters.class,
            ElectrumClient.class
    })
    @ConditionalOnMissingBean
    BitcoinjElectrumClient bitcoinjElectrumClient(NetworkParameters network,
                                                  ElectrumClient client) {
        return new BitcoinjElectrumClientImpl(network, client);
    }
}
