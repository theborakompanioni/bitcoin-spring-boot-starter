package org.tbk.spring.testcontainer.electrumx.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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

import static java.util.Objects.requireNonNull;
import static org.tbk.spring.testcontainer.core.MoreTestcontainers.buildInternalContainerUrl;

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

    @Bean(name = "electrumxContainer", destroyMethod = "stop")
    public ElectrumxContainer<?> electrumxContainer(BitcoindContainer<?> bitcoindContainer) {
        List<Integer> hardcodedStandardPorts = ImmutableList.<Integer>builder()
                .add(8000)
                .add(50001)
                .add(50002)
                .add(50004)
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

        String bitcoindDaemonUrl = buildInternalContainerUrl(
                bitcoindContainer,
                "http",
                this.properties.getRpcuser(),
                this.properties.getRpcpass(),
                this.properties.getRpcport()
        );

        ImmutableMap<String, String> env = ImmutableMap.<String, String>builder()
                .put("DAEMON_URL", bitcoindDaemonUrl)
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

        ElectrumxContainer<?> electrumxContainer = new ElectrumxContainer<>(dockerImageName)
                .withCreateContainerCmdModifier(MoreTestcontainers.cmdModifiers().withName(dockerContainerName))
                .withExposedPorts(exposedPorts.toArray(new Integer[]{}))
                .withEnv(env)
                .waitingFor(waitStrategy);

        electrumxContainer.start();

        // expose all mapped ports of the host so other containers can communication with electrumx
        MoreTestcontainers.exposeAllPortsToOtherContainers(electrumxContainer);

        return electrumxContainer;
    }
}
