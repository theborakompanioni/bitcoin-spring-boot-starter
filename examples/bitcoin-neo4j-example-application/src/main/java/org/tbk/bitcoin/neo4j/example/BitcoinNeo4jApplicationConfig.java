package org.tbk.bitcoin.neo4j.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.exchange.BitcoinStandardExchangeRateProvider;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.utility.DockerImageName;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
@Configuration
public class BitcoinNeo4jApplicationConfig {

    @Bean
    public BitcoinStandardExchangeRateProvider bitcoinStandardExchangeRateProvider() {
        return new BitcoinStandardExchangeRateProvider();
    }

    @Bean
    public org.neo4j.ogm.config.Configuration neo4jOgmConfiguration(Neo4jContainer<?> neo4jContainer,
                                                                    Neo4jProperties neo4jProperties) {
        boolean hasCorrectUsername = "neo4j".equals(neo4jProperties.getUsername());
        checkArgument(hasCorrectUsername, "Username MUST be 'neo4j' - otherwise testcontainers won't work.");

        Neo4jProperties props = new Neo4jProperties();
        props.setUri("bolt://localhost:" + neo4jContainer.getMappedPort(7687));
        props.setUsername("neo4j");
        props.setPassword(neo4jContainer.getAdminPassword());
        return props.createConfiguration();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Neo4jContainer<?> neo4jContainer(Neo4jProperties neo4jProperties) {
        DockerImageName dockerImageName = DockerImageName.parse("neo4j:4.1.3");
        return new Neo4jContainer<>(dockerImageName)
                .withAdminPassword(neo4jProperties.getPassword())
                .withExposedPorts()
                //.withDatabase(MountableFile.forClasspathResource("/test-graph.db"))
                ;
    }
}
