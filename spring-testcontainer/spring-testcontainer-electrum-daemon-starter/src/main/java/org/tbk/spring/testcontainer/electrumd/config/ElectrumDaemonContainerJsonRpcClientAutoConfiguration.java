package org.tbk.spring.testcontainer.electrumd.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.electrum.config.ElectrumDaemonJsonrpcConfigBuilderCustomizer;
import org.tbk.spring.testcontainer.electrumd.ElectrumDaemonContainer;

@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ElectrumDaemonJsonrpcConfigBuilderCustomizer.class)
@AutoConfigureAfter(ElectrumDaemonContainerAutoConfiguration.class)
public class ElectrumDaemonContainerJsonRpcClientAutoConfiguration {

    /**
     * Overwrite the default port of the rpc config as the mapping to the container
     * can only be determined during runtime.
     */
    @Bean
    @ConditionalOnSingleCandidate(ElectrumDaemonContainer.class)
    public ElectrumDaemonJsonrpcConfigBuilderCustomizer electrumDaemonJsonrpcConfigBuilderCustomizer(ElectrumDaemonContainer<?> electrumDaemonContainer) {
        return config -> config
                .host("http://" + electrumDaemonContainer.getHost())
                .port(electrumDaemonContainer.getMappedPort(config.getPort()));
    }
}
