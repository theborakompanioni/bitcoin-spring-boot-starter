package org.tbk.spring.bitcoin.neo4j.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.tbk.spring.bitcoin.neo4j.model.BlockNeoEntity;

@Slf4j
@Configuration
@EntityScan(basePackageClasses = BlockNeoEntity.class)
@EnableNeo4jRepositories(basePackageClasses = BlockNeoEntity.class)
public class BitcoinNeo4jAutoConfiguration {
}
