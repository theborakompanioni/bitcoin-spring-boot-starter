package org.tbk.bitcoin.txstats.example.model;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface TxScoreNeoRepository extends Neo4jRepository<TxScoreNeoEntity, Long> {
}
