package org.tbk.spring.testcontainer.lnd.example;

import com.msgilligan.bitcoinj.rpc.RpcConfig;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.NetworkParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.jsonrpc.config.BitcoinJsonRpcClientAutoConfigProperties;
import org.tbk.spring.testcontainer.bitcoind.BitcoindContainer;

import java.net.URI;

@Slf4j
@Configuration
public class CustomTestcontainerRpcClientConfig {

    /**
     * Overwrite the default port of the rpc config as the mapping to the container
     * can only be determined during runtime.
     */
    @Bean
    public RpcConfig bitcoinJsonRpcConfig(NetworkParameters bitcoinNetworkParameters,
                                          BitcoinJsonRpcClientAutoConfigProperties properties,
                                          BitcoindContainer<?> bitcoinContainer) {
        URI uri = URI.create(properties.getRpchost() + ":" + bitcoinContainer.getMappedPort(properties.getRpcport()));
        return new RpcConfig(bitcoinNetworkParameters, uri, properties.getRpcuser(), properties.getRpcpassword());
    }
}
