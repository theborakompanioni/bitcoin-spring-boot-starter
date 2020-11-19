package org.tbk.bitcoin.zeromq.config;

import lombok.Builder;
import lombok.Value;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;

import java.util.Optional;

@Value
@Builder
public class BitcoinZmqClientConfig {
    NetworkParameters network;

    String zmqpubhashtx; // Publishes transaction hashes
    String zmqpubhashblock; // Publishes block hashes
    String zmqpubrawblock; // Publishes raw block information
    String zmqpubrawtx; // Publishes raw transaction information

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

    public NetworkParameters getNetwork() {
        return Optional.ofNullable(network)
                .orElse(MainNetParams.get());
    }
}
