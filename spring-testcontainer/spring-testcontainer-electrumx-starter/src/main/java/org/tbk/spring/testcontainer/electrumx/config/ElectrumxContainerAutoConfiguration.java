package org.tbk.spring.testcontainer.electrumx.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import org.tbk.spring.testcontainer.electrumx.ElectrumxContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@EnableConfigurationProperties(ElectrumxContainerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.testcontainer.electrumx.enabled", havingValue = "true")
@AutoConfigureAfter(BitcoindContainerAutoConfiguration.class)
public class ElectrumxContainerAutoConfiguration {

    // currently only the image from "lukechilds" is supported
    private static final String DOCKER_IMAGE_NAME = "lukechilds/electrumx:v1.15.0";

    private static final DockerImageName dockerImageName = DockerImageName.parse(DOCKER_IMAGE_NAME);

    private final ElectrumxContainerProperties properties;

    public ElectrumxContainerAutoConfiguration(ElectrumxContainerProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean(name = "electrumxContainer", initMethod = "start", destroyMethod = "stop")
    public ElectrumxContainer<?> electrumxContainer(BitcoindContainer<?> bitcoindContainer) {
        String bitcoindHost = "host.testcontainers.internal";

        List<String> commands = ImmutableList.<String>builder()
                .addAll(buildCommandList())
                // TODO: expose ports specified via auto configuration properties
                // .add("--bitcoind.rpchost=" + bitcoindHost + ":" + bitcoindContainer.getMappedPort(18443))
                // .add("--bitcoind.zmqpubrawblock=tcp://" + bitcoindHost + ":" + bitcoindContainer.getMappedPort(28332))
                // .add("--bitcoind.zmqpubrawtx=tcp://" + bitcoindHost + ":" + bitcoindContainer.getMappedPort(28333))
                .build();

        List<Integer> hardcodedStandardPorts = ImmutableList.<Integer>builder()
                // .add(this.properties.getRpcport())
                // .add(this.properties.getRestport())
                .build();

        List<Integer> exposedPorts = ImmutableList.<Integer>builder()
                .addAll(hardcodedStandardPorts)
                // .addAll(this.properties.getExposedPorts())
                .build();

        // only wait for rpc ports - zeromq ports wont work (we can live with that for now)
        CustomHostPortWaitStrategy waitStrategy = CustomHostPortWaitStrategy.builder()
                .ports(hardcodedStandardPorts)
                .build();

        String dockerContainerName = String.format("%s-%s", dockerImageName.getUnversionedPart(),
                Integer.toHexString(System.identityHashCode(this)))
                .replace("/", "-");

        String daemonUrl = String.format("http://%s:%s@%s:%d",
                this.properties.getRpcuser(), this.properties.getRpcpass(),
                bitcoindHost, bitcoindContainer.getMappedPort(this.properties.getRpcport()));

        ImmutableMap<String, String> env = ImmutableMap.<String, String>builder()
                .put("DAEMON_URL", daemonUrl)
                // TODO: make coin configurable
                // following fails with "electrumx.lib.coins.CoinError: unknown coin BitcoinRegtest and network regtest combination"
                //.put("COIN", "BitcoinRegtest")
                //.put("NET", "regtest")
                // following fails with "electrumx.lib.coins.CoinError: unknown coin Bitcoin and network regtest combination"
                //.put("COIN", "Bitcoin")
                //.put("NET", "regtest")
                // why does this work? -> BitcoinSegwit/regtest?! wtf?
                .put("COIN", "BitcoinSegwit")
                .put("NET", "regtest")
                .build();

        return new ElectrumxContainer<>(dockerImageName)
                .withCreateContainerCmdModifier(MoreTestcontainers.cmdModifiers().withName(dockerContainerName))
                .withExposedPorts(exposedPorts.toArray(new Integer[]{}))
                .withCommand(commands.toArray(new String[]{}))
                .withEnv(env)
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
                // .addAll(requiredCommands)
                // .addAll(optionalCommands)
                // .addAll(overridingDefaultsCommands)
                // .addAll(bitcoinCommands)
        ;

        List<String> predefinedKeys = commandsBuilder.build().stream()
                .map(ElectrumXConfigEntry::valueOf)
                .flatMap(Optional::stream)
                .map(ElectrumXConfigEntry::getName)
                .collect(Collectors.toList());

        List<String> userGivenCommands = this.properties.getCommands();
        List<String> allowedUserGivenCommands = userGivenCommands.stream()
                .map(ElectrumXConfigEntry::valueOf)
                .flatMap(Optional::stream)
                .filter(it -> !predefinedKeys.contains(it.getName()))
                .map(ElectrumXConfigEntry::toCommandString)
                .collect(Collectors.toList());

        return commandsBuilder
                .addAll(allowedUserGivenCommands)
                .build();
    }

    @Value
    @Builder
    public static class ElectrumXConfigEntry {
        public static Optional<ElectrumXConfigEntry> valueOf(String command) {
            String commandPrefix = "--";
            return Optional.ofNullable(command)
                    .filter(it -> it.startsWith(commandPrefix))
                    .map(it -> it.split(commandPrefix)[1])
                    .map(it -> {
                        boolean withoutValue = !it.contains("=");
                        if (withoutValue) {
                            return ElectrumXConfigEntry.builder()
                                    .name(it)
                                    .build();
                        }

                        String[] parts = it.split("=");

                        return ElectrumXConfigEntry.builder()
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
