package org.tbk.spring.testcontainer.bitcoind.config;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.spring.testcontainer.bitcoind.BitcoindContainer;
import org.tbk.spring.testcontainer.core.CustomHostPortWaitStrategy;
import org.tbk.spring.testcontainer.core.MoreTestcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BitcoindContainerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.testcontainer.bitcoind.enabled", havingValue = "true")
public class BitcoindContainerAutoConfiguration {
    // currently only the image from "ruimarinho" is supported
    private static final String DEFAULT_DOCKER_IMAGE_NAME = "ruimarinho/bitcoin-core:0.21.1-alpine";
    private static final DockerImageName defaultDockerImageName = DockerImageName.parse(DEFAULT_DOCKER_IMAGE_NAME);

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
    @Bean(name = "bitcoindContainer", destroyMethod = "stop")
    public BitcoindContainer<?> bitcoindContainer() {
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

        DockerImageName dockerImageName = this.properties.getImage()
                .map(DockerImageName::parse)
                .orElse(defaultDockerImageName);

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
        MoreTestcontainers.exposeAllPortsToOtherContainers(bitcoindContainer);

        return bitcoindContainer;
    }

    private List<String> buildCommandList() {
        ImmutableList.Builder<String> requiredCommandsBuilder = ImmutableList.<String>builder()
                // rpcport: Listen for JSON-RPC connections on this port
                //rpcport=8282
                // port: Listen for incoming connections on non-default port. (p2p)
                //port=8888
                // listen: Accept incoming connections from peers. <-- check if this is mandatory "1"
                //.add("-listen=0")
                // rest: Accept public REST requests.
                //rest=1
                ;

        Optional.of(this.properties.getChain())
                .filter(it -> it != BitcoindContainerProperties.Chain.mainnet)
                .map(Enum::name)
                .map(String::toLowerCase)
                .map(it -> "-chain=" + it)
                .ifPresent(requiredCommandsBuilder::add);

        List<String> overridingDefaults = ImmutableList.<String>builder()
                // dns: Allow DNS lookups for -addnode, -seednode and -connect values.
                .add("-dns=" + this.properties.getCommandValueByKey("dns").orElse("0"))
                // dnsseed: Query for peer addresses via DNS lookup, if low on addresses.
                .add("-dnsseed=" + this.properties.getCommandValueByKey("dnsseed").orElse("0"))
                // upnp: Use UPnP to map the listening port.
                .add("-upnp=" + this.properties.getCommandValueByKey("upnp").orElse("0"))
                .add("-txindex=" + this.properties.getCommandValueByKey("txindex").orElse("1"))
                .add("-server=" + this.properties.getCommandValueByKey("server").orElse("1"))
                .add("-rpcbind=" + this.properties.getCommandValueByKey("rpcbind").orElse("0.0.0.0"))
                .add("-rpcallowip=" + this.properties.getCommandValueByKey("rpcallowip").orElse("0.0.0.0/0"))
                .build();

        ImmutableList.Builder<String> optionalCommandsBuilder = ImmutableList.<String>builder();
        this.properties.getRpcuser()
                .map(val -> String.format("-rpcuser=%s", val))
                .ifPresent(optionalCommandsBuilder::add);
        this.properties.getRpcpassword()
                .map(val -> String.format("-rpcpassword=%s", val))
                .ifPresent(optionalCommandsBuilder::add);

        ImmutableList.Builder<String> commandsBuilder = ImmutableList.<String>builder()
                .addAll(requiredCommandsBuilder.build())
                .addAll(overridingDefaults)
                .addAll(optionalCommandsBuilder.build());

        List<String> predefinedKeys = commandsBuilder.build().stream()
                .map(BitcoindConfigEntry::valueOf)
                .flatMap(Optional::stream)
                .map(BitcoindConfigEntry::getName)
                .collect(Collectors.toList());

        List<String> userGivenCommands = this.properties.getCommands();
        List<String> allowedUserGivenCommands = userGivenCommands.stream()
                .map(BitcoindConfigEntry::valueOf)
                .flatMap(Optional::stream)
                .filter(it -> !predefinedKeys.contains(it.getName()))
                .map(BitcoindConfigEntry::toCommandString)
                .collect(Collectors.toList());

        return commandsBuilder
                .addAll(allowedUserGivenCommands)
                .build();
    }


    @Value
    @Builder
    public static class BitcoindConfigEntry {
        public static Optional<BitcoindConfigEntry> valueOf(String command) {
            String commandPrefix = "-";
            return Optional.ofNullable(command)
                    .filter(it -> it.startsWith(commandPrefix))
                    .map(it -> it.split(commandPrefix)[1])
                    .map(it -> {
                        boolean withoutValue = !it.contains("=");
                        if (withoutValue) {
                            return BitcoindConfigEntry.builder()
                                    .name(it)
                                    .build();
                        }

                        String[] parts = it.split("=");

                        return BitcoindConfigEntry.builder()
                                .name(parts[0])
                                .value(parts[1])
                                .build();
                    });
        }

        @NonNull
        String name;

        String value;

        public Optional<String> getValue() {
            return Optional.ofNullable(value);
        }

        public String toCommandString() {
            return "-" + this.name + getValue()
                    .map(it -> "=" + it)
                    .orElse("");
        }
    }
}
