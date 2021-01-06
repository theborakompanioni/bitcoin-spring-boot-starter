package org.tbk.spring.testcontainer.btcrpcexplorer;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class BtcRpcExplorerContainer<S extends BtcRpcExplorerContainer<S>> extends GenericContainer<S> {

    public BtcRpcExplorerContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
    }
}
