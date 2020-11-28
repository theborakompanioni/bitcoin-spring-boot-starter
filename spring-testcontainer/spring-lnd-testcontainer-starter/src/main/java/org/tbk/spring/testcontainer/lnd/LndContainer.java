package org.tbk.spring.testcontainer.lnd;


import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class LndContainer<S extends LndContainer<S>> extends GenericContainer<S> {

    public LndContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
    }
}
