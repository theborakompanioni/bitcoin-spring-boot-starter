package org.tbk.spring.testcontainer.neo4j.config;

import com.github.dockerjava.api.command.CreateContainerCmd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.tbk.spring.testcontainer.core.MoreTestcontainers;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@EnableConfigurationProperties(Neo4jContainerProperties.class)
@ConditionalOnProperty(value = "org.tbk.spring.testcontainer.neo4j.enabled", havingValue = "true")
@AutoConfigureBefore(Neo4jDataAutoConfiguration.class)
public class Neo4jContainerAutoConfiguration {
    // testcontainers only supports 'neo4j' username atm.
    // throw loud and early if the spring autoconfigure properties mismatch!
    // user must enable testcontainers via `org.tbk.spring.testcontainer.neo4j.enabled: true`
    // so we can allow other usernames in production environments
    private static final String MANDATORY_USERNAME = "neo4j";

    private final Neo4jContainerProperties properties;

    public Neo4jContainerAutoConfiguration(Neo4jContainerProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @Bean
    public org.neo4j.ogm.config.Configuration neo4jOgmConfiguration(Neo4jContainer<?> neo4jContainer,
                                                                    Neo4jProperties neo4jProperties) {
        boolean hasCorrectUsername = MANDATORY_USERNAME.equals(neo4jProperties.getUsername());
        checkArgument(hasCorrectUsername, "Username MUST be 'neo4j' - otherwise testcontainers won't work.");

        Neo4jProperties props = new Neo4jProperties();
        props.setUri("bolt://localhost:" + neo4jContainer.getMappedPort(7687));
        props.setUsername(MANDATORY_USERNAME);
        props.setPassword(neo4jContainer.getAdminPassword());
        return props.createConfiguration();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Neo4jContainer<?> neo4jContainer(Neo4jProperties neo4jProperties) {
        DockerImageName dockerImageName = properties.getDockerImageName();

        String dockerContainerName = String.format("%s-%s", dockerImageName.getUnversionedPart(),
                Integer.toHexString(System.identityHashCode(this)))
                .replace("/", "-");

        return new Neo4jContainer<>(dockerImageName)
                .withCreateContainerCmdModifier(MoreTestcontainers.cmdModifiers().withName(dockerContainerName))
                .withAdminPassword(neo4jProperties.getPassword());
    }
}
