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
import org.tbk.spring.testcontainer.core.MoreTestcontainers;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
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
    @Bean(name = "bitcoinContainer", destroyMethod = "stop")
    public BitcoindContainer<?> bitcoinContainer() {
        List<String> commands = buildCommandList();

        // TODO: expose ports specified via auto configuration properties
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

        String dockerContainerName = String.format("%s-%s", dockerImageName.getUnversionedPart(),
                Integer.toHexString(System.identityHashCode(this)))
                .replace("/", "-");

        BitcoindContainer<?> bitcoindContainer = new BitcoindContainer<>(dockerImageName)
                .withCreateContainerCmdModifier(MoreTestcontainers.cmdModifiers().withName(dockerContainerName))
                .withExposedPorts(exposedPorts.toArray(new Integer[]{}))
                .withCommand(commands.toArray(new String[]{}))
                .waitingFor(waitStrategy);

        bitcoindContainer.start();

        checkState(bitcoindContainer.isRunning(), "'bitcoindContainer' must be running");

        // expose all mapped ports of the host so other containers can communication with bitcoind.
        // e.g. lnd needs access to rpc and zeromq ports.
        bitcoindContainer.getExposedPorts().stream()
                .map(bitcoindContainer::getMappedPort)
                .forEach(Testcontainers::exposeHostPorts);

        return bitcoindContainer;
    }

    private List<String> buildCommandList() {

        List<String> fixedCommands = ImmutableList.<String>builder()
                .add("-regtest=1")
                // dns: Allow DNS lookups for -addnode, -seednode and -connect values.
                .add("-dns=0")
                // dnsseed: Query for peer addresses via DNS lookup, if low on addresses.
                .add("-dnsseed=0")
                // listen: Accept incoming connections from peers.
                //.add("-listen=0")
                // port: Listen for incoming connections on non-default port. (p2p)
                //port=8888
                // rest: Accept public REST requests.
                //rest=1
                // rpcport: Listen for JSON-RPC connections on this port
                //rpcport=8282
                // upnp: Use UPnP to map the listening port.
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
