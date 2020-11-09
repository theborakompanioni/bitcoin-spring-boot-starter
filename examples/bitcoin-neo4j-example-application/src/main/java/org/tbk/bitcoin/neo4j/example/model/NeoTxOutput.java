package org.tbk.bitcoin.neo4j.example.model;

import lombok.Data;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.List;


@Data
@NodeEntity("txo")
public class NeoTxOutput {

    // id is "$tx_hash:$index"
    @Id
    private String id;

    private long index;

    private long value;

    // size in bytes
    private int size;

    @Relationship(type = "ADDRESS", direction = "OUTGOING")
    private NeoAddress address;

    @Relationship(type = "OUT", direction = "INCOMING")
    private NeoTx createdIn;

    @Relationship(type = "IN", direction = "OUTGOING")
    private NeoTx spentBy;

    public boolean isUnspent() {
        return spentBy == null;
    }
}
