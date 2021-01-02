package org.tbk.spring.testcontainer.tor;


import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class TorContainer<S extends TorContainer<S>> extends GenericContainer<S> {

    public TorContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
    }
}
