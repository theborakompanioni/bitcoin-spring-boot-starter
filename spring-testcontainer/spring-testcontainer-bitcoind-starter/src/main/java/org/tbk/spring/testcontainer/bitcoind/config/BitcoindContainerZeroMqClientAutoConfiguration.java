package org.tbk.spring.testcontainer.bitcoind.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.tbk.bitcoin.zeromq.config.BitcoinZeroMqClientAutoConfiguration;
import org.tbk.bitcoin.zeromq.config.BitcoinZmqClientConfigBuilderCustomizer;
import org.tbk.spring.testcontainer.bitcoind.BitcoindContainer;

import java.util.Optional;
import java.util.function.Function;

@Slf4j
@AutoConfiguration
@ConditionalOnClass(BitcoinZmqClientConfigBuilderCustomizer.class)
@AutoConfigureAfter(BitcoindContainerAutoConfiguration.class)
@AutoConfigureBefore(BitcoinZeroMqClientAutoConfiguration.class)
public class BitcoindContainerZeroMqClientAutoConfiguration {

    /**
     * Overwrite the default ports of the zeromq config as the mapping to the container
     * can only be determined during runtime.
     */
    @Bean
    @ConditionalOnBean(BitcoindContainer.class)
    BitcoinZmqClientConfigBuilderCustomizer bitcoinZmqClientConfigBuilderCustomizer(BitcoindContainer<?> bitcoinContainer) {

        Function<String, Optional<String>> replacePortInUrl = url -> {
            Optional<Integer> specifiedListeningPort = Optional.ofNullable(url)
                    .map(val -> val.substring(val.indexOf("//")))
                    .map(val -> val.substring(val.indexOf(":") + 1))
                    .map(val -> Integer.parseInt(val, 10));

            return specifiedListeningPort.map(port -> {
                String host = bitcoinContainer.getHost();
                Integer mappedPort = bitcoinContainer.getMappedPort(port);
                return "tcp://" + host + ":" + mappedPort;
            });
        };

        return config -> {
            Optional<String> pubRawBlockUrl = replacePortInUrl.apply(config.getZmqpubrawblock());
            Optional<String> pubHashBlockUrl = replacePortInUrl.apply(config.getZmqpubhashblock());
            Optional<String> pubRawTxUrl = replacePortInUrl.apply(config.getZmqpubrawtx());
            Optional<String> pubHashTxUrl = replacePortInUrl.apply(config.getZmqpubhashtx());

            pubRawBlockUrl.ifPresent(config::zmqpubrawblock);
            pubHashBlockUrl.ifPresent(config::zmqpubhashblock);
            pubRawTxUrl.ifPresent(config::zmqpubrawtx);
            pubHashTxUrl.ifPresent(config::zmqpubhashtx);
        };
    }
}
