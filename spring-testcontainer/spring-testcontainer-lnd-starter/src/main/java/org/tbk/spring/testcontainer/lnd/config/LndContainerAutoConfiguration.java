package org.tbk.spring.testcontainer.lnd.config;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.google.common.collect.ImmutableList;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.spring.testcontainer.bitcoind.BitcoindContainer;
import org.tbk.spring.testcontainer.bitcoind.config.BitcoindContainerAutoConfiguration;
import org.tbk.spring.testcontainer.core.MoreTestcontainers;
import org.tbk.spring.testcontainer.lnd.LndContainer;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@EnableConfigurationProperties(LndContainerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.testcontainer.lnd.enabled", havingValue = "true")
@AutoConfigureAfter(BitcoindContainerAutoConfiguration.class)
public class LndContainerAutoConfiguration {

    // currently only the image from "lnzap" is supported
    private static final String DOCKER_IMAGE_NAME = "lnzap/lnd:0.11.1-beta";

    private static final DockerImageName dockerImageName = DockerImageName.parse(DOCKER_IMAGE_NAME);

    private final LndContainerProperties properties;

    public LndContainerAutoConfiguration(LndContainerProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean(name = "lndContainer", initMethod = "start", destroyMethod = "stop")
    public LndContainer<?> lndContainer(BitcoindContainer<?> bitcoinContainer) {

        Testcontainers.exposeHostPorts(bitcoinContainer.getMappedPort(18443));
        Testcontainers.exposeHostPorts(bitcoinContainer.getMappedPort(28332));
        Testcontainers.exposeHostPorts(bitcoinContainer.getMappedPort(28333));

        String bitcoindHost = "host.testcontainers.internal";

        List<String> commands = ImmutableList.<String>builder()
                .addAll(buildCommandList())
                .add("--bitcoind.rpchost=" + bitcoindHost + ":" + bitcoinContainer.getMappedPort(18443))
                .add("--bitcoind.zmqpubrawblock=tcp://" + bitcoindHost + ":" + bitcoinContainer.getMappedPort(28332))
                .add("--bitcoind.zmqpubrawtx=tcp://" + bitcoindHost + ":" + bitcoinContainer.getMappedPort(28333))
                .build();

        List<Integer> hardcodedStandardPorts = ImmutableList.<Integer>builder()
                .add(this.properties.getRpcport())
                .add(this.properties.getRestport())
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

        return new LndContainer<>(dockerImageName)
                .withCreateContainerCmdModifier(MoreTestcontainers.cmdModifiers().withName(dockerContainerName))
                .withExposedPorts(exposedPorts.toArray(new Integer[]{}))
                .withCommand(commands.toArray(new String[]{}))
                .waitingFor(waitStrategy);
    }

    /**
     * Build command list.
     * e.g. see https://github.com/lightningnetwork/lnd/blob/master/sample-lnd.conf
     *
     * @return
     */
    private List<String> buildCommandList() {

        List<String> requiredCommands = ImmutableList.<String>builder()
                .add("--noseedbackup") // so no create/unlock wallet is needed ("lncli unlock")
                //.add("--listen=9735")
                //.add("--externalip=127.0.0.1:9735")
                .add("--restlisten=0.0.0.0:" + this.properties.getRestport())
                .add("--rpclisten=0.0.0.0:" + this.properties.getRpcport())
                .build();

        List<String> optionalCommands = ImmutableList.<String>builder()
                .add("--alias=" + this.properties.getCommandValueByKey("alias").orElse("tbk-lnd-testcontainer-regtest"))
                .add("--color=" + this.properties.getCommandValueByKey("color").orElse("#eeeeee"))
                .add("--debuglevel=" + this.properties.getCommandValueByKey("debuglevel").orElse("debug"))
                .add("--trickledelay=" + this.properties.getCommandValueByKey("trickledelay").orElse("1000"))
                .build();

        List<String> overridingDefaultsCommands = ImmutableList.<String>builder()
                .add("--maxpendingchannels=" + this.properties.getCommandValueByKey("maxpendingchannels").orElse("10"))
                //.add("--autopilot.active=false") <-- fails with "bool flag `--autopilot.active' cannot have an argument"
                //.add("protocol.wumbo-channels=1")
                .build();

        List<String> bitcoinCommands = ImmutableList.<String>builder()
                .add("--bitcoin.active")
                .add("--bitcoin.regtest")
                .add("--bitcoin.node=bitcoind")
                .add("--bitcoin.defaultchanconfs=" + this.properties.getCommandValueByKey("bitcoin.defaultchanconfs").orElse("1"))
                .build();

        ImmutableList.Builder<String> commandsBuilder = ImmutableList.<String>builder()
                .addAll(requiredCommands)
                .addAll(optionalCommands)
                .addAll(overridingDefaultsCommands)
                .addAll(bitcoinCommands);

        List<String> predefinedKeys = commandsBuilder.build().stream()
                .map(LndConfigEntry::valueOf)
                .flatMap(Optional::stream)
                .map(LndConfigEntry::getName)
                .collect(Collectors.toList());

        List<String> userGivenCommands = this.properties.getCommands();
        List<String> allowedUserGivenCommands = userGivenCommands.stream()
                .map(LndConfigEntry::valueOf)
                .flatMap(Optional::stream)
                .filter(it -> !predefinedKeys.contains(it.getName()))
                .map(LndConfigEntry::toCommandString)
                .collect(Collectors.toList());

        return commandsBuilder
                .addAll(allowedUserGivenCommands)
                .build();
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


    @Value
    @Builder
    public static class LndConfigEntry {
        public static Optional<LndConfigEntry> valueOf(String command) {
            String commandPrefix = "--";
            return Optional.ofNullable(command)
                    .filter(it -> it.startsWith(commandPrefix))
                    .map(it -> it.split(commandPrefix)[1])
                    .map(it -> {
                        boolean withoutValue = !it.contains("=");
                        if (withoutValue) {
                            return LndConfigEntry.builder()
                                    .name(it)
                                    .build();
                        }

                        String[] parts = it.split("=");

                        return LndConfigEntry.builder()
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
            return "--" + this.name + getValue().map(it -> "=" + it).orElse("");
        }
    }

}
