package org.tbk.spring.bitcoin.neo4j.model;

import lombok.Data;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Required;


@Data
@NodeEntity("txo")
public class TxOutputNeoEntity {

    // id is "$tx_hash:$index"
    @Id
    private String id;

    @Required
    private long index;

    @Required
    private long value;

    // size in bytes
    private int size;

    @Relationship(type = "ADDRESS", direction = "OUTGOING")
    private AddressNeoEntity address;

    @Required
    @Relationship(type = "OUT", direction = "INCOMING")
    private TxNeoEntity createdIn;

    @Relationship(type = "IN", direction = "OUTGOING")
    private TxNeoEntity spentBy;

    public boolean isUnspent() {
        return spentBy == null;
    }
}
