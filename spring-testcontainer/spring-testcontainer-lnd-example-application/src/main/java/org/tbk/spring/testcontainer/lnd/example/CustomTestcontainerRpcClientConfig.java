package org.tbk.spring.testcontainer.lnd.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.tbk.bitcoin.jsonrpc.config.RpcConfigBuilderCustomizer;
import org.tbk.spring.testcontainer.bitcoind.BitcoindContainer;

@Slf4j
@Configuration
public class CustomTestcontainerRpcClientConfig {

    /**
     * Overwrite the default port of the rpc config as the mapping to the container
     * can only be determined during runtime.
     */
    @Bean
    public RpcConfigBuilderCustomizer bitcoinJsonRpcConfigBuilderCustomizer(BitcoindContainer<?> bitcoinContainer) {
        return config -> {
            UriComponents uriComponents = UriComponentsBuilder.fromUri(config.getUri())
                    .port(bitcoinContainer.getMappedPort(config.getUri().getPort()))
                    .build();

            config.uri(uriComponents.toUri());
        };
    }
}
