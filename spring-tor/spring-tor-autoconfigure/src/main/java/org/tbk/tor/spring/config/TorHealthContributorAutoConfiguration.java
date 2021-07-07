package org.tbk.tor.spring.config;

import com.google.common.collect.ImmutableMap;
import com.runjva.sourceforge.jsocks.protocol.Socks5Proxy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.berndpruenster.netlayer.tor.HiddenServiceSocket;
import org.berndpruenster.netlayer.tor.Tor;
import org.berndpruenster.netlayer.tor.TorCtlException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthContributorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.info.ConditionalOnEnabledInfoContributor;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.PingHealthIndicator;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.tor.hs.HiddenServiceDefinition;
import org.tbk.tor.spring.actuate.health.HiddenServiceHealthIndicator;
import org.tbk.tor.spring.actuate.health.HiddenServiceSocketHealthIndicator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "org.tbk.tor.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({
        HealthContributor.class,
        HiddenServiceDefinition.class
})
@AutoConfigureAfter({
        TorHiddenServiceAutoConfiguration.class,
        TorAutoConfiguration.class,
        TorHttpClientAutoConfiguration.class
})
public class TorHealthContributorAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(HiddenServiceDefinition.class)
    @ConditionalOnEnabledHealthIndicator("hiddenService")
    @AutoConfigureAfter({
            TorHiddenServiceAutoConfiguration.class,
            TorHttpClientAutoConfiguration.class
    })
    // cannot be an inner static class - would not be picked up by spring correctly
    public class TorHiddenServiceHealthContributorAutoConfiguration extends
            CompositeHealthContributorConfiguration<HiddenServiceHealthIndicator, HiddenServiceDefinition> {

        private final CloseableHttpClient torHttpClient;

        public TorHiddenServiceHealthContributorAutoConfiguration(@Qualifier("torHttpClient") CloseableHttpClient torHttpClient) {
            this.torHttpClient = requireNonNull(torHttpClient);
        }

        @Override
        protected HiddenServiceHealthIndicator createIndicator(HiddenServiceDefinition bean) {
            return new HiddenServiceHealthIndicator(bean, this.torHttpClient);
        }

        @Bean
        @ConditionalOnMissingBean(name = {"hiddenServiceHealthIndicator", "hiddenServiceHealthContributor"})
        public HealthContributor hiddenServiceHealthContributor(Map<String, HiddenServiceDefinition> hiddenServices) {
            if (hiddenServices.isEmpty()) {
                return new PingHealthIndicator();
            }
            return createContributor(hiddenServices);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnEnabledHealthIndicator("hiddenServiceSocket")
    @ConditionalOnBean(HiddenServiceSocket.class)
    @AutoConfigureAfter({
            TorAutoConfiguration.class
    })
    public class TorHiddenServiceSocketHealthContributorAutoConfiguration extends
            CompositeHealthContributorConfiguration<HiddenServiceSocketHealthIndicator, HiddenServiceSocket> {

        @Bean
        @ConditionalOnMissingBean(name = {"hiddenServiceSocketHealthIndicator", "hiddenServiceSocketHealthContributor"})
        public HealthContributor hiddenServiceSocketHealthContributor(Map<String, HiddenServiceSocket> hiddenServiceSockets) {
            if (hiddenServiceSockets.isEmpty()) {
                return new PingHealthIndicator();
            }
            return createContributor(hiddenServiceSockets);
        }
    }

    @Bean
    @ConditionalOnSingleCandidate(Tor.class)
    @ConditionalOnEnabledInfoContributor("tor")
    @ConditionalOnMissingBean(name = "torInfoContributor")
    public InfoContributor torInfoContributor(Tor tor) {
        return builder -> {
            Optional<Socks5Proxy> proxyOrEmpty = Optional.of(tor).map(it -> {
                try {
                    return tor.getProxy();
                } catch (TorCtlException e) {
                    return null;
                }
            });

            ImmutableMap.Builder<String, Object> detailBuilder = ImmutableMap.<String, Object>builder()
                    .put("proxy_available", proxyOrEmpty.isPresent());

            proxyOrEmpty.ifPresent(it -> {
                detailBuilder.put("proxy_port", it.getPort());
                detailBuilder.put("proxy_address", it.getInetAddress());
            });

            builder.withDetail("tor", detailBuilder
                    .build());
        };
    }

    @Bean
    @ConditionalOnBean(HiddenServiceDefinition.class)
    @ConditionalOnEnabledInfoContributor("tor")
    @ConditionalOnMissingBean(name = "hiddenServiceInfoContributor")
    public InfoContributor hiddenServiceInfoContributor(List<HiddenServiceDefinition> hiddenServices) {
        return builder -> {
            Map<String, Object> hiddenServiceInfos = hiddenServices.stream()
                    .collect(Collectors.toMap(HiddenServiceDefinition::getName, val -> {
                        return ImmutableMap.<String, Object>builder()
                                .put("name", val.getName())
                                .put("virtual_host", val.getVirtualHost().orElse("unknown"))
                                .put("virtual_port", val.getVirtualPort())
                                .put("host", val.getHost())
                                .put("port", val.getPort())
                                .build();
                    }));

            builder.withDetails(ImmutableMap.<String, Object>builder()
                    .put("hiddenService", hiddenServiceInfos)
                    .build());
        };
    }
}

