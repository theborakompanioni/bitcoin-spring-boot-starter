package org.tbk.spring.bitcoin.neo4j.model;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface TxOutputNeoRepository extends Neo4jRepository<TxOutputNeoEntity, String> {
}
