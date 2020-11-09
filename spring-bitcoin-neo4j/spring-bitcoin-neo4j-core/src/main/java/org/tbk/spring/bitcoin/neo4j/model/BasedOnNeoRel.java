package org.tbk.spring.bitcoin.neo4j.model;

import lombok.Data;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;


@Data
@RelationshipEntity(type = "BASED_ON")
public class BasedOnNeoRel {

    @StartNode
    private BlockNeoEntity block;

    @EndNode
    private BlockNeoEntity prevblock;
}
