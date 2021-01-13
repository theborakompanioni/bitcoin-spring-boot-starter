package org.tbk.tor;

import org.berndpruenster.netlayer.tor.NativeTor;
import org.berndpruenster.netlayer.tor.TorCtlException;
import reactor.core.publisher.Mono;

import java.io.File;

public class NativeTorFactory implements TorFactory<NativeTor> {

    @Override
    public Mono<NativeTor> create() {
        return Mono.create(fluxSink -> {
            try {
                File workingDirectory = new File("tor-working-dir");
                fluxSink.success(new NativeTor(workingDirectory));
            } catch (TorCtlException e) {
                fluxSink.error(e);
            }
        });
    }
}
