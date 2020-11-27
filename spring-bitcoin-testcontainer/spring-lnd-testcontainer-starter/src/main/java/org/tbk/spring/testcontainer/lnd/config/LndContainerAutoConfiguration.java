package org.tbk.spring.testcontainer.lnd.config;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.spring.bitcoin.testcontainer.config.BitcoinContainerAutoConfiguration;
import org.tbk.spring.bitcoin.testcontainer.config.BitcoinContainerProperties;
import org.tbk.spring.testcontainer.bitcoind.BitcoindContainer;
import org.tbk.spring.testcontainer.lnd.LndContainer;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@EnableConfigurationProperties(LndContainerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.lnd.testcontainer.enabled", havingValue = "true")
@AutoConfigureAfter(BitcoinContainerAutoConfiguration.class)
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

//        String bitcoindHost = bitcoinContainer.getHost();
        String bitcoindHost = "host.testcontainers.internal";


        List<String> commands = ImmutableList.<String>builder()
                .addAll(buildCommandList())
                .add("--bitcoind.rpchost=" + bitcoindHost + ":" + bitcoinContainer.getMappedPort(18443))
                .add("--bitcoind.zmqpubrawblock=tcp://" + bitcoindHost + ":" + bitcoinContainer.getMappedPort(28332))
                .add("--bitcoind.zmqpubrawtx=tcp://" + bitcoindHost + ":" + bitcoinContainer.getMappedPort(28333))
                .build();

        List<Integer> hardcodedStandardPorts = ImmutableList.<Integer>builder()
                .add(8080)
                .add(10009)
                .build();

        List<Integer> exposedPorts = ImmutableList.<Integer>builder()
                .addAll(hardcodedStandardPorts)
                .addAll(this.properties.getExposedPorts())
                .build();

        // only wait for rpc ports - zeromq ports wont work (we can live with that for now)
        CustomHostPortWaitStrategy waitStrategy = CustomHostPortWaitStrategy.builder()
                .ports(hardcodedStandardPorts)
                .build();

        return new LndContainer<>(dockerImageName)
                .withExposedPorts(exposedPorts.toArray(new Integer[]{}))
                .withCommand(commands.toArray(new String[]{}))
                .waitingFor(waitStrategy)
                ;
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
                .add("--restlisten=0.0.0.0:8080")
                .add("--rpclisten=0.0.0.0:10009")
                .build();

        List<String> optionalCommands = ImmutableList.<String>builder()
                .add("--alias=tbk-lnd-testcontainer-regtest")
                .add("--color=#eeeeee")
                .add("--debuglevel=debug")
                .add("--trickledelay=1000")
                .build();

        List<String> overridingDefaultsCommands = ImmutableList.<String>builder()
                .add("--maxpendingchannels=10")
                //.add("--autopilot.active=false") <-- fails with "bool flag `--autopilot.active' cannot have an argument"
                .add("protocol.wumbo-channels=1")
                .build();

        List<String> bitcoinCommands = ImmutableList.<String>builder()
                .add("--bitcoin.active")
                .add("--bitcoin.regtest")
                .add("--bitcoin.node=bitcoind")
                .add("--bitcoin.defaultchanconfs=1")
                .build();

        ImmutableList.Builder<String> commandsBuilder = ImmutableList.<String>builder()
                .addAll(requiredCommands)
                .addAll(optionalCommands)
                .addAll(overridingDefaultsCommands)
                .addAll(bitcoinCommands);

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
