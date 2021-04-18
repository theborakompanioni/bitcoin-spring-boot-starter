package org.tbk.mqtt.moquette.config;

import com.google.common.collect.ImmutableList;
import io.moquette.BrokerConstants;
import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MoquetteBrokerAutoConfigProperties.class)
@ConditionalOnProperty(value = "org.tbk.mqtt.moquette.broker.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(Server.class)
public class MoquetteBrokerAutoConfiguration {

    private static final List<String> typesafeMoquettePropertyNames = ImmutableList.<String>builder()
            .add(BrokerConstants.HOST_PROPERTY_NAME)
            .add(BrokerConstants.PORT_PROPERTY_NAME)
            .add(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME)
            .build();

    private final MoquetteBrokerAutoConfigProperties properties;

    public MoquetteBrokerAutoConfiguration(MoquetteBrokerAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public MemoryConfig moquetteConfig() {
        Properties moquetteProperties = moquetteProperties();

        typesafeMoquettePropertyNames.stream()
                .filter(moquetteProperties::containsKey)
                .forEach(it -> log.warn("Moqette config key '{}' will be overwritten with its type-safe alternative. " +
                        "To get rid of this message remove this key from your config.", it));

        MemoryConfig config = new MemoryConfig(moquetteProperties);
        config.setProperty(BrokerConstants.HOST_PROPERTY_NAME, this.properties.getHost());
        config.setProperty(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(this.properties.getPort()));
        config.setProperty(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, this.properties.getWebsocketPort()
                .map(it -> Integer.toString(it))
                .orElse(BrokerConstants.DISABLED_PORT_BIND));

        return config;
    }

    private Properties moquetteProperties() {
        Properties moquetteProperties = new Properties();
        this.properties.getConfig().forEach(moquetteProperties::setProperty);
        return moquetteProperties;
    }

    @Bean(destroyMethod = "stopServer")
    @ConditionalOnMissingBean
    public Server moquetteServer(MemoryConfig moquetteConfig) throws IOException {
        Server server = new Server();
        server.startServer(moquetteConfig);
        return server;
    }
}
