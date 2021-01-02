package org.tbk.spring.testcontainer.tor.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.spring.testcontainer.tor.TorContainer;
import org.tbk.spring.testcontainer.tor.TorHttpClientBuilder;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;

@Slf4j
@Configuration
@ConditionalOnClass(CloseableHttpClient.class)
@AutoConfigureAfter(TorContainerAutoConfiguration.class)
public class TorHttpClientAutoConfiguration {

    @Bean(name = "torHttpClient", destroyMethod = "close")
    @ConditionalOnBean(TorContainer.class)
    @ConditionalOnMissingBean(name = "torHttpClient")
    public CloseableHttpClient torHttpClient(TorContainer<?> torContainer) {
        SocketAddress sockAddr = new InetSocketAddress("localhost", torContainer.getMappedPort(9050));
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, sockAddr);

        return TorHttpClientBuilder.custom(proxy)
                .build();
    }
}
