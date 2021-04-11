package org.tbk.bitcoin.zeromq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Optional;

@Data
@ConfigurationProperties(
        prefix = "org.tbk.bitcoin.zeromq",
        ignoreUnknownFields = false
)
public class BitcoinZeroMqClientAutoConfigurationProperties {

    private boolean enabled;

    private Network network;

    private String zmqpubhashtx; // Publishes transaction hashes
    private String zmqpubhashblock; // Publishes block hashes
    private String zmqpubrawblock; // Publishes raw block information
    private String zmqpubrawtx; // Publishes raw transaction information


    public Optional<String> getZmqpubhashtx() {
        return Optional.ofNullable(zmqpubhashtx);
    }

    public Optional<String> getZmqpubhashblock() {
        return Optional.ofNullable(zmqpubhashblock);
    }

    public Optional<String> getZmqpubrawblock() {
        return Optional.ofNullable(zmqpubrawblock);
    }

    public Optional<String> getZmqpubrawtx() {
        return Optional.ofNullable(zmqpubrawtx);
    }

    public Network getNetwork() {
        return Optional.ofNullable(network)
                .orElse(Network.mainnet);
    }
}
