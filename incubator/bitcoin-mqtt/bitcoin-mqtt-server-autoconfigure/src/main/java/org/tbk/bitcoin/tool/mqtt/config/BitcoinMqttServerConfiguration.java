package org.tbk.bitcoin.tool.mqtt.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.tool.mqtt.BitcoinMqttServer;
import org.tbk.mqtt.moquette.config.MoquetteBrokerAutoConfiguration;

import static java.util.Objects.requireNonNull;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BitcoinMqttServerAutoConfigProperties.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.tool.mqtt.server.enabled", havingValue = "true")
@AutoConfigureAfter(MoquetteBrokerAutoConfiguration.class)
@ConditionalOnClass(BitcoinMqttServer.class)
public class BitcoinMqttServerConfiguration {

    private final BitcoinMqttServerAutoConfigProperties properties;

    public BitcoinMqttServerConfiguration(BitcoinMqttServerAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }
}
