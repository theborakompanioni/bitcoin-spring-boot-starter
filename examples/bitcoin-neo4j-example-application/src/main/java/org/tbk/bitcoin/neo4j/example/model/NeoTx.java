package org.tbk.bitcoin.neo4j.example.model;

import lombok.Data;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.List;

@Data
@NodeEntity("tx")
public class NeoTx {
    @Id
    private String txid;

    @Relationship(type = "INCLUDED_IN")
    private NeoBlock block;

    @Relationship(type = "IN", direction = "INCOMING")
    private List<NeoTxOutput> inputs;

    @Relationship(type = "OUT", direction = "OUTGOING")
    private List<NeoTxOutput> outputs;
}

