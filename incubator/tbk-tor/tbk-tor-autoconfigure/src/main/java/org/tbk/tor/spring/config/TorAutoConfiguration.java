package org.tbk.tor.spring.config;

import com.google.common.collect.ImmutableMap;
import org.apache.http.impl.client.CloseableHttpClient;
import org.berndpruenster.netlayer.tor.HiddenServiceSocket;
import org.berndpruenster.netlayer.tor.NativeTor;
import org.berndpruenster.netlayer.tor.Tor;
import org.berndpruenster.netlayer.tor.TorCtlException;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.tor.NativeTorFactory;
import org.tbk.tor.TorFactory;
import org.tbk.tor.hs.DefaultTorHiddenServiceFactory;
import org.tbk.tor.hs.TorHiddenServiceFactory;
import org.tbk.tor.http.SimpleTorHttpClientBuilder;

import java.time.Duration;
import java.util.Optional;

@Configuration
public class TorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TorFactory<NativeTor> nativeTorFactory() {
        return new NativeTorFactory();
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public NativeTor nativeTor(TorFactory<NativeTor> nativeTorFactory) {
        NativeTor tor = nativeTorFactory.create().blockOptional(Duration.ofSeconds(60))
                .orElseThrow(() -> new IllegalStateException("Could not start tor"));

        if (Tor.getDefault() == null) {
            // set default instance, so it can be omitted whenever creating Tor (Server)Sockets
            Tor.setDefault(tor);
        }

        return tor;
    }

    @Bean
    @ConditionalOnMissingBean
    public TorHiddenServiceFactory torHiddenServiceFactory(Tor tor) {
        return new DefaultTorHiddenServiceFactory(tor);
    }

    @Bean(name = "torHttpClient", destroyMethod = "close")
    @ConditionalOnMissingBean(name = "torHttpClient")
    public CloseableHttpClient torHttpClient(Tor tor) throws TorCtlException {
        return SimpleTorHttpClientBuilder.tor(tor)
                .build();
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnWebApplication
    @ConditionalOnBean(ServerProperties.class)
    public HiddenServiceSocket defaultHiddenServiceSocket(ServerProperties serverProperties,
                                                          ApplicationContext applicationContext,
                                                          TorHiddenServiceFactory torHiddenServiceFactory) {
        int port = Optional.ofNullable(serverProperties.getPort())
                .filter(it -> it > 0)
                .orElseThrow(() -> new IllegalStateException("Could not find local port to create hidden service"));

        // create a hidden service in directory 'test' inside the tor installation directory
        TorHiddenServiceFactory.HiddenServiceCreateContext context = TorHiddenServiceFactory.HiddenServiceCreateContext.builder()
                .internalPort(port + 1)
                .hiddenServiceDir("application")
                .hiddenServicePort(port)
                .build();

        return torHiddenServiceFactory.createReady(context).blockOptional(Duration.ofSeconds(60))
                .orElseThrow(() -> new IllegalStateException("Could not start hidden service"));
    }

    @Bean
    @ConditionalOnBean(HiddenServiceSocket.class)
    @ConditionalOnMissingBean(name = "defaultHiddenServiceSocketInfoContributor")
    public InfoContributor defaultHiddenServiceSocketInfoContributor(HiddenServiceSocket defaultHiddenServiceSocket) {
        return builder -> builder.withDetail("defaultHiddenService", ImmutableMap.<String, Object>builder()
                .put("name", defaultHiddenServiceSocket.getServiceName())
                .put("address", defaultHiddenServiceSocket.getSocketAddress())
                .put("local_address", defaultHiddenServiceSocket.getLocalSocketAddress())
                .build());
    }
}
