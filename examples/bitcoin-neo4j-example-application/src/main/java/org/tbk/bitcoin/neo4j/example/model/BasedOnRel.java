package org.tbk.bitcoin.neo4j.example.model;

import lombok.Data;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;


@Data
@RelationshipEntity(type = "BASED_ON")
public class BasedOnRel {

    @StartNode
    private NeoBlock block;

    @EndNode
    private NeoBlock prevblock;
}
