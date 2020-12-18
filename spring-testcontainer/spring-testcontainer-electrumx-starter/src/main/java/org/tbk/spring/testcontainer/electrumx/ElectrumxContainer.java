package org.tbk.spring.testcontainer.electrumx;


import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class ElectrumxContainer<S extends ElectrumxContainer<S>> extends GenericContainer<S> {

    public ElectrumxContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
    }
}
