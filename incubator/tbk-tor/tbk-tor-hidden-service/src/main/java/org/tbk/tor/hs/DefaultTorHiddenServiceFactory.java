package org.tbk.tor.hs;

import org.berndpruenster.netlayer.tor.HiddenServiceSocket;
import org.berndpruenster.netlayer.tor.Tor;
import reactor.core.publisher.Mono;

import static java.util.Objects.requireNonNull;

public class DefaultTorHiddenServiceFactory implements TorHiddenServiceFactory {

    private final Tor tor;

    public DefaultTorHiddenServiceFactory(Tor tor) {
        this.tor = requireNonNull(tor);
    }

    @Override
    public HiddenServiceSocket create(HiddenServiceCreateContext context) {
        return new HiddenServiceSocket(
                context.getInternalPort(),
                context.getHiddenServiceDir(),
                context.getHiddenServicePort(),
                context.getTor().orElse(this.tor)
        );
    }

    @Override
    public Mono<HiddenServiceSocket> createReady(HiddenServiceCreateContext context) {
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
