package org.tbk.spring.testcontainer.bitcoind.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.tbk.bitcoin.jsonrpc.config.BitcoinJsonRpcClientAutoConfiguration;
import org.tbk.bitcoin.jsonrpc.config.RpcConfigBuilderCustomizer;
import org.tbk.spring.testcontainer.bitcoind.BitcoindContainer;

@Slf4j
@AutoConfiguration
@ConditionalOnClass(RpcConfigBuilderCustomizer.class)
@AutoConfigureAfter(BitcoindContainerAutoConfiguration.class)
@AutoConfigureBefore(BitcoinJsonRpcClientAutoConfiguration.class)
public class BitcoindContainerJsonRpcClientAutoConfiguration {

    /**
     * Overwrite the default port of the rpc config as the mapping to the container
     * can only be determined during runtime.
     */
    @Bean
    @ConditionalOnBean(BitcoindContainer.class)
    RpcConfigBuilderCustomizer bitcoinJsonRpcConfigBuilderCustomizer(BitcoindContainer<?> bitcoinContainer) {
        return config -> config
                .host("http://" + bitcoinContainer.getHost())
                .port(bitcoinContainer.getMappedPort(config.getPort()));
    }
}
