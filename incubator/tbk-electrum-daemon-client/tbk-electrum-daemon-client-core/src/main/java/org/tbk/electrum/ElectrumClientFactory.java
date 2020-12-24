package org.tbk.electrum;

import java.net.URI;

public interface ElectrumClientFactory {
    ElectrumClient create(URI uri, String username, String password);
}
