package org.tbk.spring.bitcoin.testcontainer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

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

        return new GenericContainer<>(imageName)
                .withExposedPorts(18443, 18444)
                .withCommand(
                        "-printtoconsole",
                        "-regtest=1",
                        "-dnsseed=0",
                        "-upnp=0",
                        "-txindex=1",
                        "-server=1",
                        "-rpcbind=0.0.0.0",
                        "-rpcallowip=0.0.0.0/0",
                        "-rpcuser=myrpcuser",
                        "-rpcpassword=correcthorsebatterystaple");
    }
}
