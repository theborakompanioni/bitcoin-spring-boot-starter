package org.tbk.bitcoin.neo4j.example.model;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface TransactionRepository extends Neo4jRepository<NeoTx, String> {
}
