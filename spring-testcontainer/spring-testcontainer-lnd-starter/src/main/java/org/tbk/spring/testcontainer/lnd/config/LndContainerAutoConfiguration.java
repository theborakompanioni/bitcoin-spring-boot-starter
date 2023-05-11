package org.tbk.spring.testcontainer.lnd.config;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.spring.testcontainer.bitcoind.BitcoindContainer;
import org.tbk.spring.testcontainer.bitcoind.config.BitcoindContainerAutoConfiguration;
import org.tbk.spring.testcontainer.core.CustomHostPortWaitStrategy;
import org.tbk.spring.testcontainer.core.MoreTestcontainers;
import org.tbk.spring.testcontainer.lnd.LndContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.tbk.spring.testcontainer.core.MoreTestcontainers.buildInternalContainerUrl;
import static org.tbk.spring.testcontainer.core.MoreTestcontainers.buildInternalContainerUrlWithoutProtocol;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LndContainerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.testcontainer.lnd.enabled", havingValue = "true")
@AutoConfigureAfter(BitcoindContainerAutoConfiguration.class)
public class LndContainerAutoConfiguration {

    private static final String DOCKER_IMAGE_NAME = "lightninglabs/lnd:v0.16.2-beta";

    private static final DockerImageName dockerImageName = DockerImageName.parse(DOCKER_IMAGE_NAME);

    private final LndContainerProperties properties;

    public LndContainerAutoConfiguration(LndContainerProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean(name = "lndContainer", initMethod = "start", destroyMethod = "stop")
    public LndContainer<?> lndContainer(BitcoindContainer<?> bitcoindContainer) {
        List<String> commands = ImmutableList.<String>builder()
                .addAll(buildCommandList())
                // TODO: expose ports specified via auto configuration properties
                .add("--bitcoind.rpchost=" + buildInternalContainerUrlWithoutProtocol(bitcoindContainer, 18443))
                .add("--bitcoind.zmqpubrawblock=" + buildInternalContainerUrl(bitcoindContainer, "tcp", 28332))
                .add("--bitcoind.zmqpubrawtx=" + buildInternalContainerUrl(bitcoindContainer, "tcp", 28333))
                .build();

        List<Integer> hardcodedStandardPorts = ImmutableList.<Integer>builder()
                .add(this.properties.getRpcport())
                .add(this.properties.getRestport())
                .build();

        List<Integer> exposedPorts = ImmutableList.<Integer>builder()
                .addAll(hardcodedStandardPorts)
                .addAll(this.properties.getExposedPorts())
                .build();

        // only wait for rpc ports - zeromq ports won't work (we can live with that for now)
        WaitStrategy portWaitStrategy = CustomHostPortWaitStrategy.builder()
                .ports(hardcodedStandardPorts)
                .build();

        WaitStrategy waitStrategy = new WaitAllStrategy(WaitAllStrategy.Mode.WITH_OUTER_TIMEOUT)
                .withStrategy(portWaitStrategy)
                .withStrategy(Wait.forLogMessage(".*Opened wallet.*", 1))
                .withStartupTimeout(LndContainerProperties.DEFAULT_STARTUP_TIMEOUT);

        String dockerContainerName = String.format("%s-%s", dockerImageName.getUnversionedPart(),
                        Integer.toHexString(System.identityHashCode(this)))
                .replace("/", "-");

        return new LndContainer<>(dockerImageName)
                .dependsOn(bitcoindContainer)
                .withCreateContainerCmdModifier(MoreTestcontainers.cmdModifiers().withName(dockerContainerName))
                .withExposedPorts(exposedPorts.toArray(new Integer[]{}))
                .withCommand(commands.toArray(new String[]{}))
                .waitingFor(waitStrategy);
    }

    /**
     * Build command list.
     * e.g. see https://github.com/lightningnetwork/lnd/blob/master/sample-lnd.conf
     *
     * @return a list fo commands for the container.
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
                .add("--alias=" + this.properties.getCommandValueByKey("alias").orElse("tbk-lnd-regtest"))
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
                .toList();

        List<String> userGivenCommands = this.properties.getCommands();
        List<String> allowedUserGivenCommands = userGivenCommands.stream()
                .map(LndConfigEntry::valueOf)
                .flatMap(Optional::stream)
                .filter(it -> !predefinedKeys.contains(it.getName()))
                .map(LndConfigEntry::toCommandString)
                .toList();

        return commandsBuilder
                .addAll(allowedUserGivenCommands)
                .build();
    }

    @Value
    @Builder
    public static class LndConfigEntry {
        public static Optional<LndConfigEntry> valueOf(String command) {
            String commandPrefix = "--";
            return Optional.ofNullable(command)
                    .filter(it -> it.startsWith(commandPrefix))
                    .map(it -> it.split(commandPrefix, 2)[1])
                    .map(it -> {
                        boolean withoutValue = !it.contains("=");
                        if (withoutValue) {
                            return LndConfigEntry.builder()
                                    .name(it)
                                    .build();
                        }

                        String[] parts = it.split("=", 2);

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
            return "--" + this.name + getValue()
                    .map(it -> "=" + it)
                    .orElse("");
        }
    }

}
