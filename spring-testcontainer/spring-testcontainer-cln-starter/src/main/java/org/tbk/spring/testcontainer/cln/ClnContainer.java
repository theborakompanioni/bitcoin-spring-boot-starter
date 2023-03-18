package org.tbk.spring.testcontainer.cln;


import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class ClnContainer<S extends ClnContainer<S>> extends GenericContainer<S> {

    public ClnContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
    }

}
