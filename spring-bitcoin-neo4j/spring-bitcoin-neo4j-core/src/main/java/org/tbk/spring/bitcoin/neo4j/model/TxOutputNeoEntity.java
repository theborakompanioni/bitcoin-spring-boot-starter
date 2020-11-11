package org.tbk.spring.bitcoin.neo4j.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.neo4j.ogm.annotation.*;

import java.util.HashMap;
import java.util.Map;


@Data
@EqualsAndHashCode(of = "id")
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

    @Properties(prefix = "meta")
    private Map<String, Object> meta = new HashMap<>();

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
