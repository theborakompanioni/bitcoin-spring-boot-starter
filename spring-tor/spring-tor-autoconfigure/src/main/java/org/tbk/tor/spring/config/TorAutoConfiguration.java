package org.tbk.tor.spring.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.berndpruenster.netlayer.tor.NativeTor;
import org.berndpruenster.netlayer.tor.Tor;
import org.berndpruenster.netlayer.tor.Torrc;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.tor.NativeTorFactory;
import org.tbk.tor.TorFactory;
import org.tbk.tor.hs.DefaultTorHiddenServiceSocketFactory;
import org.tbk.tor.hs.HiddenServiceDefinition;
import org.tbk.tor.hs.TorHiddenServiceSocketFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration(proxyBeanMethods = false)
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

        torrcEntries.putAll(createTorrcEntriesFromProperties(this.properties));

        List<ImmutableMap<String, String>> torrcEntriesFromBeans = hiddenServices.stream()
                .map(it -> {
                    String hiddenServicePort = String.format("%d %s:%d", it.getVirtualPort(), it.getHost(), it.getPort());

                    return ImmutableMap.<String, String>builder()
                            .put("HiddenServiceDir", it.getDirectory().getAbsolutePath())
                            .put("HiddenServicePort", hiddenServicePort)
                            .build();
                }).collect(Collectors.toList());

        torrcEntriesFromBeans.forEach(torrcEntries::putAll);

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
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
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

    private Map<String, String> createTorrcEntriesFromProperties(TorAutoConfigProperties props) {
        return ImmutableMap.<String, String>builder()
                .build();
    }
}
