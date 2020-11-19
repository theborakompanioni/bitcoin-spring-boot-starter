package org.tbk.spring.bitcoin.testcontainer.config;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@EnableConfigurationProperties(BitcoinContainerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.bitcoin.testcontainer.enabled", havingValue = "true")
public class BitcoinContainerAutoConfiguration {

    private final BitcoinContainerProperties properties;

    public BitcoinContainerAutoConfiguration(BitcoinContainerProperties properties) {
        this.properties = requireNonNull(properties);
    }

    /**
     * Mainnet
     * JSON-RPC/REST: 8332
     * P2P: 8333
     * <p>
     * Testnet
     * Testnet JSON-RPC: 18332
     * P2P: 18333
     * <p>
     * Regtest
     * JSON-RPC/REST: 18443 (since 0.16+, otherwise 18332)
     * P2P: 18444
     */
    @Bean(name = "bitcoinContainer", initMethod = "start", destroyMethod = "stop")
    public GenericContainer<?> bitcoinContainer() {
        DockerImageName imageName = DockerImageName.parse("ruimarinho/bitcoin-core:0.20.1-alpine");

        List<String> commands = ImmutableList.<String>builder()
                .add("-printtoconsole")
                .add("-regtest=1")
                .add("-dnsseed=0")
                .add("-upnp=0")
                .add("-txindex=1")
                .add("-server=1")
                .add("-rpcbind=0.0.0.0")
                .add("-rpcallowip=0.0.0.0/0")
                .build();

        List<String> commandsWithUserValues = ImmutableList.<String>builder()
                .addAll(commands)
                .add(String.format("-rpcuser=%s", this.properties.getRpcuser()))
                .add(String.format("-rpcpassword=%s", this.properties.getRpcpassword()))
                .build();

        return new GenericContainer<>(imageName)
                .withExposedPorts(18443, 18444)
                .withCommand(commandsWithUserValues.toArray(new String[]{}));
    }
}
