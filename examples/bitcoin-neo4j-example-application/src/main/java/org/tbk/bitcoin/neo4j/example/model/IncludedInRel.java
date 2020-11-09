package org.tbk.bitcoin.neo4j.example.model;

import lombok.Data;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;


@Data
@RelationshipEntity(type = "INCLUDED_IN")
public class IncludedInRel {

    @StartNode
    private NeoTx transaction;

    @EndNode
    private NeoBlock block;
}
