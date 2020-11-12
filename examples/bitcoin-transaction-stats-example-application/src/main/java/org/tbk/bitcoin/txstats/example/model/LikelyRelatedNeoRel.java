package org.tbk.bitcoin.txstats.example.model;

import lombok.Data;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.tbk.spring.bitcoin.neo4j.model.TxNeoEntity;
import org.tbk.spring.bitcoin.neo4j.model.TxOutputNeoEntity;

@Data
@RelationshipEntity(type = "LIKELY_RELATED")
public class LikelyRelatedNeoRel {

    @StartNode
    private TxOutputNeoEntity entity0;

    @EndNode
    private TxOutputNeoEntity entity1;
}
