package org.tbk.mqtt.moquette.config;

import io.moquette.BrokerConstants;
import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MoquetteBrokerAutoConfigProperties.class)
@ConditionalOnProperty(value = "org.tbk.mqtt.moquette.broker.enabled", havingValue = "true")
@ConditionalOnClass(Server.class)
public class MoquetteBrokerAutoConfiguration {

    private final MoquetteBrokerAutoConfigProperties properties;

    public MoquetteBrokerAutoConfiguration(MoquetteBrokerAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public MemoryConfig moquetteConfig() {
        MemoryConfig config = new MemoryConfig(new Properties());
        config.setProperty(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(BrokerConstants.PORT));
        config.setProperty(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, Integer.toString(BrokerConstants.WEBSOCKET_PORT));
        config.setProperty(BrokerConstants.HOST_PROPERTY_NAME, BrokerConstants.HOST);
        config.setProperty(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.toString(false));

        return config;
    }

    @Bean(destroyMethod = "stopServer")
    @ConditionalOnMissingBean
    public Server moquetteServer(MemoryConfig moquetteConfig) throws IOException {
        Server server = new Server();
        server.startServer(moquetteConfig);
        return server;
    }
}
