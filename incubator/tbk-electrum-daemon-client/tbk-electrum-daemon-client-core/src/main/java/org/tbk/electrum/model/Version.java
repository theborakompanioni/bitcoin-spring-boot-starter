package org.tbk.electrum.model;

import java.util.Optional;

public interface Version {

    String getVersion();

    Optional<SemanticVersion> getSemanticVersion();

}
