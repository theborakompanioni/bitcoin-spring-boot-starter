package org.tbk.tor.spring.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.berndpruenster.netlayer.tor.Tor;
import org.berndpruenster.netlayer.tor.TorCtlException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.tor.http.SimpleTorHttpClientBuilder;

@Configuration
@ConditionalOnProperty(value = "org.tbk.tor.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(SimpleTorHttpClientBuilder.class)
@AutoConfigureAfter(TorAutoConfiguration.class)
public class TorHttpClientAutoConfiguration {

    @Bean(name = "torHttpClient", destroyMethod = "close")
    @ConditionalOnMissingBean(name = "torHttpClient")
    public CloseableHttpClient torHttpClient(Tor tor,
                                             ObjectProvider<TorHttpClientBuilderCustomizer> torHttpClientBuilderCustomizers)
            throws TorCtlException {
        HttpClientBuilder torHttpClientBuilder = SimpleTorHttpClientBuilder.tor(tor);

        torHttpClientBuilderCustomizers.orderedStream().forEach(customizer -> customizer.customize(torHttpClientBuilder));

        return torHttpClientBuilder
                .build();
    }

    /**
     * Callback interface that can be implemented by beans wishing to customize Tor Http Client config
     * {@link HttpClientBuilder} before it is used.
     */
    @FunctionalInterface
    public interface TorHttpClientBuilderCustomizer {

        /**
         * Customize the tor http client config.
         *
         * @param config the {@link HttpClientBuilder} to customize
         */
        void customize(HttpClientBuilder config);

    }

}
