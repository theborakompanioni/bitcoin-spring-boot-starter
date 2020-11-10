package org.tbk.spring.bitcoin.neo4j.model;

import lombok.Data;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;


@Data
@RelationshipEntity(type = "PREV_BLOCK")
public class PrevBlockNeoRel {

    @StartNode
    private BlockNeoEntity block;

    @EndNode
    private BlockNeoEntity prevblock;
}
