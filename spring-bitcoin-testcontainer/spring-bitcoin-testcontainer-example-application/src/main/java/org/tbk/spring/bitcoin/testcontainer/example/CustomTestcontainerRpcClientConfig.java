package org.tbk.spring.bitcoin.testcontainer.example;

import com.msgilligan.bitcoinj.rpc.RpcConfig;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.NetworkParameters;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.tbk.bitcoin.jsonrpc.config.BitcoinJsonRpcClientAutoConfigProperties;
import org.testcontainers.containers.GenericContainer;

import java.net.URI;

@Slf4j
@EnableScheduling
@Configuration
public class CustomTestcontainerRpcClientConfig {

    /**
     * Overwrite the default port of the rpc config as the mapping to the container
     * can only be determined during runtime.
     */
    @Bean
    public RpcConfig bitcoinJsonRpcConfig(NetworkParameters bitcoinNetworkParameters,
                                          BitcoinJsonRpcClientAutoConfigProperties properties,
                                          @Qualifier("bitcoinContainer") GenericContainer<?> bitcoinContainer) {
        URI uri = URI.create(properties.getRpchost() + ":" + bitcoinContainer.getMappedPort(properties.getRpcport()));
        return new RpcConfig(bitcoinNetworkParameters, uri, properties.getRpcuser(), properties.getRpcpassword());
    }
}
