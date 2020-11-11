package org.tbk.spring.bitcoin.neo4j.model;

import lombok.Data;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
@NodeEntity("addr")
public class AddressNeoEntity {

    @Id
    private String address;

    @Relationship(type = "ADDRESS", direction = "INCOMING")
    private List<TxOutputNeoEntity> outputs;

    @Properties(prefix = "meta")
    private Map<String, Object> meta = new HashMap<>();
}
