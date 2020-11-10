package org.tbk.bitcoin.txstats.example.model;

import lombok.Data;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.tbk.spring.bitcoin.neo4j.model.TxNeoEntity;

@Data
@NodeEntity("tx_score")
public class TxScoreNeoEntity {
    @Id
    @GeneratedValue
    private Long id;

    private long score;
    private boolean finalized;

    private String type;

    @Relationship(type = "SCORES")
    private TxNeoEntity tx;
}

