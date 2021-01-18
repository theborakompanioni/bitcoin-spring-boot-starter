package org.tbk.tor.spring.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.runjva.sourceforge.jsocks.protocol.Socks5Proxy;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.berndpruenster.netlayer.tor.NativeTor;
import org.berndpruenster.netlayer.tor.Tor;
import org.berndpruenster.netlayer.tor.TorCtlException;
import org.berndpruenster.netlayer.tor.Torrc;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.tbk.tor.NativeTorFactory;
import org.tbk.tor.TorFactory;
import org.tbk.tor.hs.DefaultTorHiddenServiceSocketFactory;
import org.tbk.tor.hs.HiddenServiceDefinition;
import org.tbk.tor.hs.TorHiddenServiceSocketFactory;
import org.tbk.tor.http.SimpleTorHttpClientBuilder;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@EnableConfigurationProperties(TorAutoConfigProperties.class)
@ConditionalOnProperty(value = "org.tbk.tor.enabled", havingValue = "true", matchIfMissing = true)
public class TorAutoConfiguration {

    private final TorAutoConfigProperties properties;

    public TorAutoConfiguration(TorAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnBean(HiddenServiceDefinition.class)
    @ConditionalOnMissingBean
    public Torrc torrcWithHiddenServiceDefinitions(List<HiddenServiceDefinition> hiddenServices) throws IOException {
        LinkedHashMap<String, String> torrcEntries = Maps.newLinkedHashMap();

        hiddenServices.forEach(it -> {
            String hiddenServicePort = String.format("%d %s:%d", it.getVirtualPort(), it.getHost(), it.getPort());

            torrcEntries.put("HiddenServiceDir", it.getDirectory().getAbsolutePath());
            torrcEntries.put("HiddenServicePort", hiddenServicePort);
        });

        torrcEntries.putAll(createTorrcEntriesFromProperties(this.properties));

        return new Torrc(torrcEntries);
    }

    @Bean
    @ConditionalOnMissingBean
    public Torrc torrc() throws IOException {
        LinkedHashMap<String, String> torrcEntries = Maps.newLinkedHashMap();

        torrcEntries.putAll(createTorrcEntriesFromProperties(this.properties));

        return new Torrc(torrcEntries);
    }

    @Bean
    @ConditionalOnMissingBean(Tor.class)
    public TorFactory<NativeTor> nativeTorFactory(Torrc torrc) {
        File workingDirectory = new File(properties.getWorkingDirectory());
        return new NativeTorFactory(workingDirectory, torrc);
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(Tor.class)
    public NativeTor nativeTor(TorFactory<NativeTor> nativeTorFactory) {
        NativeTor tor = nativeTorFactory.create()
                .blockOptional(properties.getStartupTimeout())
                .orElseThrow(() -> new IllegalStateException("Could not start tor"));

        if (Tor.getDefault() == null) {
            // set default instance, so it can be omitted whenever creating Tor (Server)Sockets
            Tor.setDefault(tor);
        }

        return tor;
    }

    @Bean
    @ConditionalOnMissingBean
    public TorHiddenServiceSocketFactory torHiddenServiceSocketFactory(Tor tor) {
        return new DefaultTorHiddenServiceSocketFactory(tor);
    }

    @Configuration
    @ConditionalOnClass(SimpleTorHttpClientBuilder.class)
    @AutoConfigureAfter(TorAutoConfiguration.class)
    public static class TorHttpClientAutoConfiguration {
        @Bean(name = "torHttpClient", destroyMethod = "close")
        @ConditionalOnMissingBean(name = "torHttpClient")
        public CloseableHttpClient torHttpClient(Tor tor) throws TorCtlException {
            return SimpleTorHttpClientBuilder.tor(tor)
                    .build();
        }
    }

    @Bean
    @ConditionalOnBean(HiddenServiceDefinition.class)
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
                    .put("hidden_services", hiddenServiceInfos)
                    .build());
        };
    }

    @Bean
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

    private LinkedHashMap<String, String> createTorrcEntriesFromProperties(TorAutoConfigProperties props) {
        LinkedHashMap<String, String> map = Maps.newLinkedHashMap();
        props.getHiddenServices().forEach((name, spec) -> {
            String hiddenServiceDir = String.format("%s/hiddenservice/%s", props.getWorkingDirectory(), spec.getDirectory());
            String hiddenServicePort = String.format("%d %s:%d", spec.getVirtualPort(), spec.getHost(), spec.getPort());

            map.put("HiddenServiceDir", new File(hiddenServiceDir).getAbsolutePath());
            map.put("HiddenServicePort", hiddenServicePort);
        });
        return map;
    }

}
