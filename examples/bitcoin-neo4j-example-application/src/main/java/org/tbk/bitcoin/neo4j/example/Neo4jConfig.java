package org.tbk.bitcoin.neo4j.example;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.neo4j.annotation.EnableNeo4jAuditing;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configurable
@EnableNeo4jRepositories
@EnableNeo4jAuditing
@EnableTransactionManagement
public class Neo4jConfig {
}
