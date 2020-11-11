package org.tbk.spring.bitcoin.neo4j.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(of = "txid")
@NodeEntity("tx")
public class TxNeoEntity {
    @Id
    private String txid;

    /**
     * Transaction data format version (note, this is signed)
     */
    private long version;

    /**
     * Number of Transaction inputs (never zero)
     */
    private long txincount;

    /**
     * Number of Transaction outputs
     */
    private long txoutcount;

    private long locktime;

    @Properties(prefix = "meta")
    private Map<String, Object> meta = new HashMap<>();

    @Relationship(type = "INCLUDED_IN")
    private BlockNeoEntity block;

    @Relationship(type = "IN", direction = "INCOMING")
    private List<TxOutputNeoEntity> inputs;

    @Relationship(type = "OUT", direction = "OUTGOING")
    private List<TxOutputNeoEntity> outputs;
}

