package org.tbk.spring.testcontainer.electrumd;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class ElectrumDaemonContainer<S extends ElectrumDaemonContainer<S>> extends GenericContainer<S> {

    public ElectrumDaemonContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
    }
}
