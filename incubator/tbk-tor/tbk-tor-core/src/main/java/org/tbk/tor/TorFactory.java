package org.tbk.tor;

import org.berndpruenster.netlayer.tor.Tor;
import reactor.core.publisher.Mono;

public interface TorFactory<T extends Tor> {

    Mono<T> create();
}
