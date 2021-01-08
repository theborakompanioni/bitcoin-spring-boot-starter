package org.tbk.tor;

import org.berndpruenster.netlayer.tor.Tor;

public interface TorFactory<T extends Tor> {

    T create();
}
