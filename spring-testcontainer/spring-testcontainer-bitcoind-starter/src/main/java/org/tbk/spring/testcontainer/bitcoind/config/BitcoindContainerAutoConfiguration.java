package org.tbk.spring.testcontainer.bitcoind.config;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.spring.testcontainer.bitcoind.BitcoindContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@EnableConfigurationProperties(BitcoindContainerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.testcontainer.bitcoind.enabled", havingValue = "true")
public class BitcoindContainerAutoConfiguration {
    // currently only the image from "ruimarinho" is supported
    private static final String DOCKER_IMAGE_NAME = "ruimarinho/bitcoin-core:0.20.1-alpine";
    private static final DockerImageName dockerImageName = DockerImageName.parse(DOCKER_IMAGE_NAME);

    private final BitcoindContainerProperties properties;

    public BitcoindContainerAutoConfiguration(BitcoindContainerProperties properties) {
        this.properties = requireNonNull(properties);
    }


    /**
     * Creates a bitcoin container from the properties given.
     * <p>
     * NOTE: Currently only supports creating "regtest" container.
     * <p>
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
    public BitcoindContainer<?> bitcoinContainer() {
        List<String> commands = buildCommandList();

        List<Integer> hardcodedStandardPorts = ImmutableList.<Integer>builder()
                .add(18443)
                .add(18444)
                .build();

        List<Integer> exposedPorts = ImmutableList.<Integer>builder()
                .addAll(hardcodedStandardPorts)
                .addAll(this.properties.getExposedPorts())
                .build();

        // only wait for rpc ports - zeromq ports wont work (we can live with that for now)
        CustomHostPortWaitStrategy waitStrategy = CustomHostPortWaitStrategy.builder()
                .ports(hardcodedStandardPorts)
                .build();

        return new BitcoindContainer<>(dockerImageName)
                .withExposedPorts(exposedPorts.toArray(new Integer[]{}))
                .withCommand(commands.toArray(new String[]{}))
                .waitingFor(waitStrategy);
    }

    private List<String> buildCommandList() {
        List<String> fixedCommands = ImmutableList.<String>builder()
                .add("-regtest=1")
                .add("-dnsseed=0")
                .add("-upnp=0")
                .add("-txindex=1")
                .add("-server=1")
                .add("-rpcbind=0.0.0.0")
                .add("-rpcallowip=0.0.0.0/0")
                .build();

        ImmutableList.Builder<String> commandsBuilder = ImmutableList.<String>builder()
                .addAll(fixedCommands);

        this.properties.getRpcuser()
                .map(val -> String.format("-rpcuser=%s", val))
                .ifPresent(commandsBuilder::add);
        this.properties.getRpcpassword()
                .map(val -> String.format("-rpcpassword=%s", val))
                .ifPresent(commandsBuilder::add);

        commandsBuilder.addAll(this.properties.getCommands());

        return commandsBuilder.build();
    }

    @Value
    @Builder
    @EqualsAndHashCode(callSuper = false)
    public static class CustomHostPortWaitStrategy extends HostPortWaitStrategy {
        @Singular("addPort")
        List<Integer> ports;

        @Override
        protected Set<Integer> getLivenessCheckPorts() {
            return ports.stream()
                    .map(val -> waitStrategyTarget.getMappedPort(val))
                    .collect(Collectors.toSet());
        }
    }
}
