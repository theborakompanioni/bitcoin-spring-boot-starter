package org.tbk.bitcoin.tool.mqtt.config;

import io.moquette.broker.Server;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Transaction;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.tool.mqtt.BitcoinMqttServer;
import org.tbk.bitcoin.tool.mqtt.BitcoinMqttServerImpl;
import org.tbk.bitcoin.zeromq.client.MessagePublishService;
import org.tbk.bitcoin.zeromq.config.BitcoinjZeroMqClientAutoConfiguration;
import org.tbk.mqtt.moquette.config.MoquetteBrokerAutoConfiguration;

import static java.util.Objects.requireNonNull;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BitcoinMqttServerAutoConfigProperties.class)
@ConditionalOnProperty(value = "org.tbk.bitcoin.tool.mqtt.server.enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter({
        BitcoinjZeroMqClientAutoConfiguration.class,
        MoquetteBrokerAutoConfiguration.class
})
@ConditionalOnClass(BitcoinMqttServer.class)
public class BitcoinMqttServerConfiguration {

    private final BitcoinMqttServerAutoConfigProperties properties;

    public BitcoinMqttServerConfiguration(BitcoinMqttServerAutoConfigProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean(destroyMethod = "stopAsync")
    @Conditional(BitcoinMqttServerBeanCondition.class)
    public BitcoinMqttServer bitcoinMqttServer(Server mqttServer,
                                               MessagePublishService<Block> blockMessagePublisherService,
                                               MessagePublishService<Transaction> transactionMessagePublisherService) {
        BitcoinMqttServerImpl bitcoinMqttServer = new BitcoinMqttServerImpl(this.properties.getClientId(), mqttServer, blockMessagePublisherService, transactionMessagePublisherService);
        bitcoinMqttServer.startAsync();
        return bitcoinMqttServer;
    }

    private static class BitcoinMqttServerBeanCondition extends AnyNestedCondition {

        BitcoinMqttServerBeanCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnBean(Server.class)
        static class MoquetteServerBeanPresent {

        }

        @ConditionalOnBean(value = Block.class, parameterizedContainer = MessagePublishService.class)
        static class BlockMessagePublishServiceBeanPresent {

        }

        @ConditionalOnBean(value = Transaction.class, parameterizedContainer = MessagePublishService.class)
        static class TransactionMessagePublishServiceBeanPresent {

        }

    }
}
