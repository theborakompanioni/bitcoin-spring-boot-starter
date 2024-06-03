package org.tbk.spring.testcontainer.bitcoind.config;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.tbk.spring.testcontainer.bitcoind.BitcoindContainer;
import org.tbk.spring.testcontainer.core.CustomHostPortWaitStrategy;
import org.tbk.spring.testcontainer.core.MoreTestcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(BitcoindContainerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.testcontainer.bitcoind.enabled", havingValue = "true")
public class BitcoindContainerAutoConfiguration {

    private final BitcoindContainerProperties properties;

    public BitcoindContainerAutoConfiguration(BitcoindContainerProperties properties) {
        this.properties = requireNonNull(properties);
    }

    /**
     * Creates a bitcoin container from the properties given.
     *
     * <p>NOTE: Currently only really supports creating "regtest" container.
     * Defaults do not play nicely with mainnet/testnet -> "networkactive" is disabled.
     *
     * <p>Mainnet
     * JSON-RPC/REST: 8332
     * P2P: 8333
     *
     * <p>Testnet
     * Testnet JSON-RPC: 18332
     * P2P: 18333
     *
     * <p>Regtest
     * JSON-RPC/REST: 18443 (since 0.16+, otherwise 18332)
     * P2P: 18444
     *
     * <p>Signet
     * JSON-RPC/REST: 38332
     * P2P: 38333
     */
    @Bean(name = "bitcoindContainer", destroyMethod = "stop")
    BitcoindContainer<?> bitcoindContainer() {
        List<String> commands = buildCommandList();

        List<Integer> exposedPorts = ImmutableList.<Integer>builder()
                .add(this.properties.getRpcport())
                .add(this.properties.getPort())
                .addAll(this.properties.getExposedPorts())
                .build();

        // only wait for rpc ports - p2p ports might be disabled (networkactive=0); zeromq ports won't work (we can live with that for now)
        CustomHostPortWaitStrategy waitStrategy = CustomHostPortWaitStrategy.builder()
                .ports(ImmutableList.<Integer>builder()
                        .add(this.properties.getRpcport())
                        .build())
                .build();

        DockerImageName dockerImageName = this.properties.getImage()
                .orElseThrow(() -> new RuntimeException("Container image must not be empty"));

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
                .add("-chain=" + this.properties.getNetwork().getChain())
                .add("-rpcport=" + this.properties.getRpcport())
                .add("-port=" + this.properties.getPort());

        List<String> overridingDefaults = ImmutableList.<String>builder()
                // dns: Allow DNS lookups for -addnode, -seednode and -connect values.
                .add("-dns=" + this.properties.getCommandValueByKey("dns").orElse("0"))
                // dnsseed: Query for peer addresses via DNS lookup, if low on addresses.
                .add("-dnsseed=" + this.properties.getCommandValueByKey("dnsseed").orElse("0"))
                // upnp: Use UPnP to map the listening port.
                .add("-upnp=" + this.properties.getCommandValueByKey("upnp").orElse("0"))
                // networkactive: enable all P2P network activity.
                .add("-networkactive=" + this.properties.getCommandValueByKey("networkactive").orElse("0"))
                .add("-txindex=" + this.properties.getCommandValueByKey("txindex").orElse("1"))
                .add("-server=" + this.properties.getCommandValueByKey("server").orElse("1"))
                .add("-rpcbind=" + this.properties.getCommandValueByKey("rpcbind").orElse("0.0.0.0"))
                .add("-rpcallowip=" + this.properties.getCommandValueByKey("rpcallowip").orElse("0.0.0.0/0"))
                .build();

        ImmutableList.Builder<String> optionalCommandsBuilder = ImmutableList.builder();
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
                .toList();

        List<String> userGivenCommands = this.properties.getCommands();
        List<String> allowedUserGivenCommands = userGivenCommands.stream()
                .map(BitcoindConfigEntry::valueOf)
                .flatMap(Optional::stream)
                .filter(it -> !predefinedKeys.contains(it.getName()))
                .map(BitcoindConfigEntry::toCommandString)
                .toList();

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
                    .map(it -> it.split(commandPrefix, 2)[1])
                    .map(it -> {
                        boolean withoutValue = !it.contains("=");
                        if (withoutValue) {
                            return BitcoindConfigEntry.builder()
                                    .name(it)
                                    .build();
                        }

                        String[] parts = it.split("=", 2);

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
