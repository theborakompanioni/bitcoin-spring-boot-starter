package org.tbk.spring.bitcoin.neo4j.model;

import lombok.Data;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.List;

@Data
@NodeEntity("tx")
public class TxNeoEntity {
    @Id
    private String txid;

    @Relationship(type = "INCLUDED_IN")
    private BlockNeoEntity block;

    @Relationship(type = "IN", direction = "INCOMING")
    private List<TxOutputNeoEntity> inputs;

    @Relationship(type = "OUT", direction = "OUTGOING")
    private List<TxOutputNeoEntity> outputs;
}

