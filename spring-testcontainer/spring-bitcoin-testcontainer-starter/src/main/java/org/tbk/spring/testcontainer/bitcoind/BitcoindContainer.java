package org.tbk.spring.testcontainer.bitcoind;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class BitcoindContainer<S extends BitcoindContainer<S>> extends GenericContainer<S> {

    public BitcoindContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
    }
}
