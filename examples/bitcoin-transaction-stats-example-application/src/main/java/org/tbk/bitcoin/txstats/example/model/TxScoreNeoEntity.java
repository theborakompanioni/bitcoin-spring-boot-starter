package org.tbk.bitcoin.txstats.example.model;

import com.google.common.collect.Lists;
import lombok.Data;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.annotation.typeconversion.DateString;
import org.tbk.spring.bitcoin.neo4j.model.TxNeoEntity;

import java.time.Instant;
import java.util.List;

@Data
@NodeEntity("tx_score")
public class TxScoreNeoEntity {
    @Id
    @GeneratedValue
    private Long id;

    @Required
    @DateString
    private Instant createdAt;

    private long score;

    private boolean finalized;

    @Required
    private String type;

    private List<String> labels = Lists.newArrayList();

    @Required
    @Relationship(type = "SCORES")
    private TxNeoEntity tx;
}

