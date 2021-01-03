package org.tbk.spring.testcontainer.tor;

import java.util.Optional;

public interface HiddenServiceHostnameResolver {
    Optional<String> findHiddenServiceUrl(String serviceName);
}
