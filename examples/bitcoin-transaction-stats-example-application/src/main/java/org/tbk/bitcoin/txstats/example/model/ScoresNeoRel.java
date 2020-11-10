package org.tbk.bitcoin.txstats.example.model;

import lombok.Data;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.tbk.spring.bitcoin.neo4j.model.AddressNeoEntity;
import org.tbk.spring.bitcoin.neo4j.model.TxNeoEntity;

@Data
@RelationshipEntity(type = "SCORES")
public class ScoresNeoRel {

    @StartNode
    private TxScoreNeoEntity source;

    @EndNode
    private TxNeoEntity target;
}
