package org.tbk.bitcoin.neo4j.example.model;

import lombok.Data;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.List;


@Data
@NodeEntity("addr")
public class NeoAddress {

    @Id
    private String address;

    @Relationship(type = "ADDRESS", direction = "INCOMING")
    private List<NeoTxOutput> outputs;
}
