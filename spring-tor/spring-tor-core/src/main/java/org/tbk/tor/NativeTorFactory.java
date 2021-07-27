package org.tbk.tor;

import org.berndpruenster.netlayer.tor.NativeTor;
import org.berndpruenster.netlayer.tor.TorCtlException;
import org.berndpruenster.netlayer.tor.Torrc;
import reactor.core.publisher.Mono;

import java.io.File;

import static java.util.Objects.requireNonNull;

public final class NativeTorFactory implements TorFactory<NativeTor> {

    private final File workingDirectory;
    private final Torrc torrc;

    public NativeTorFactory(File workingDirectory) {
        this(workingDirectory, null);
    }

    public NativeTorFactory(File workingDirectory, Torrc torrc) {
        this.workingDirectory = requireNonNull(workingDirectory);
        this.torrc = torrc;
    }

    @Override
    public Mono<NativeTor> create() {
        return Mono.create(fluxSink -> {
            try {
                fluxSink.success(new NativeTor(workingDirectory, null, torrc));
            } catch (TorCtlException e) {
                fluxSink.error(e);
            }
        });
    }
}
