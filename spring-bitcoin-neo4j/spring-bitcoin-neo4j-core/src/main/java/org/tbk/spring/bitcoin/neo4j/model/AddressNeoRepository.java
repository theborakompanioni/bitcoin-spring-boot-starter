package org.tbk.spring.bitcoin.neo4j.model;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface AddressNeoRepository extends Neo4jRepository<AddressNeoEntity, String> {
}
