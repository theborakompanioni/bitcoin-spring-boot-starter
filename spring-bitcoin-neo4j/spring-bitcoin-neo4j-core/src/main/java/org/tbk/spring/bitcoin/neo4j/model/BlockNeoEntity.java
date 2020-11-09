package org.tbk.spring.bitcoin.neo4j.model;

import lombok.Data;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.List;

@Data
@NodeEntity("block")
public class BlockNeoEntity {
    @Id
    private String hash;

    /*int version;

    String prevblock;
    String merkeroot;
    long timestamp;

    String bits;
    long nonce;
    int size;
    int txcount;*/

    @Relationship(type = "BASED_ON")
    private BlockNeoEntity prevblock;

    @Relationship(type = "BASED_ON", direction = "INCOMING")
    private BlockNeoEntity nextblock;

    @Relationship(type = "INCLUDED_IN", direction = "INCOMING")
    private List<TxNeoEntity> transactions = new ArrayList<>();
}

