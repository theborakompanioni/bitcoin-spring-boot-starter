package org.tbk.bitcoin.neo4j.example.model;

import lombok.Data;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.List;

@Data
@NodeEntity("block")
public class NeoBlock {
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
    private NeoBlock prevblock;

    @Relationship(type = "BASED_ON", direction = "INCOMING")
    private NeoBlock nextblock;

    @Relationship(type = "INCLUDED_IN", direction = "INCOMING")
    private List<NeoTx> transactions = new ArrayList<>();
}

