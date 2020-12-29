package org.tbk.spring.testcontainer.eps;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class ElectrumPersonalServerContainer<S extends ElectrumPersonalServerContainer<S>> extends GenericContainer<S> {

    public ElectrumPersonalServerContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
    }
}
