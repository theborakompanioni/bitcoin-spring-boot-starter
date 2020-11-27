package org.tbk.spring.bitcoin.testcontainer.example;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.NetworkParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.tbk.bitcoin.zeromq.config.BitcoinZmqClientConfig;
import org.tbk.spring.bitcoin.testcontainer.config.BitcoinContainerProperties;
import org.tbk.spring.testcontainer.bitcoind.BitcoindContainer;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@EnableScheduling
@Configuration
public class CustomTestcontainerZeroMqClientConfig {

    /**
     * Overwrite the default ports of the zeromq config as the mapping to the container
     * can only be determined during runtime.
     */
    @Bean
    public BitcoinZmqClientConfig bitcoinZmqClientConfig(
            NetworkParameters bitcoinNetworkParameters,
            BitcoindContainer<?> bitcoinContainer,
            BitcoinContainerProperties bitcoinContainerProperties) {

        List<String> customZeroMqSettings = bitcoinContainerProperties.getCommands().stream()
                .filter(val -> val.startsWith("-zmqpub"))
                .collect(Collectors.toList());

        Function<String, Optional<String>> replacePortInUrl = name -> {
            Optional<Integer> specifiedListeningPort = customZeroMqSettings.stream()
                    .filter(val -> val.startsWith("-" + name + "="))
                    .map(val -> val.substring(val.indexOf("=")))
                    .map(val -> val.substring(val.indexOf("//")))
                    .map(val -> val.substring(val.indexOf(":") + 1))
                    .map(val -> Integer.parseInt(val, 10))
                    .findFirst();

            return specifiedListeningPort.map(port -> {
                String host = bitcoinContainer.getHost();
                Integer mappedPort = bitcoinContainer.getMappedPort(port);
                return "tcp://" + host + ":" + mappedPort;
            });
        };

        Optional<String> pubRawBlockUrl = replacePortInUrl.apply("zmqpubrawblock");
        Optional<String> pubRawTxUrl = replacePortInUrl.apply("zmqpubrawtx");
        Optional<String> pubHashBlockUrl = replacePortInUrl.apply("zmqpubhashtx");
        Optional<String> pubHashTxUrl = replacePortInUrl.apply("zmqpubhashtx");

        return BitcoinZmqClientConfig.builder()
                .network(bitcoinNetworkParameters)
                .zmqpubrawblock(pubRawBlockUrl.orElse(null))
                .zmqpubrawtx(pubRawTxUrl.orElse(null))
                .zmqpubhashblock(pubHashBlockUrl.orElse(null))
                .zmqpubhashtx(pubHashTxUrl.orElse(null))
                .build();
    }
}
