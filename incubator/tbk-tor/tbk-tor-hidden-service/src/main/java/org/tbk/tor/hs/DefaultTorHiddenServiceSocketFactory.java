package org.tbk.tor.hs;

import org.berndpruenster.netlayer.tor.HiddenServiceSocket;
import org.berndpruenster.netlayer.tor.Tor;
import reactor.core.publisher.Mono;

import static java.util.Objects.requireNonNull;

public class DefaultTorHiddenServiceSocketFactory implements TorHiddenServiceSocketFactory {

    private final Tor tor;

    public DefaultTorHiddenServiceSocketFactory(Tor tor) {
        this.tor = requireNonNull(tor);
    }

    @Override
    public HiddenServiceSocket create(HiddenServiceSocketCreateContext context) {
        return new HiddenServiceSocket(
                context.getHostPort(),
                context.getHiddenServiceDir(),
                context.getVirtualPort(),
                context.getTor().orElse(this.tor)
        );
    }

    @Override
    public Mono<HiddenServiceSocket> createReady(HiddenServiceSocketCreateContext context) {
        return Mono.create(fluxSink -> {
            HiddenServiceSocket hiddenServiceSocket = this.create(context);
            // it takes some time for a hidden service to be ready, so adding a listener after creating should not be an issue
            hiddenServiceSocket.addReadyListener(socket -> {
                fluxSink.success(hiddenServiceSocket);
                return null;
            });
        });
    }
}
