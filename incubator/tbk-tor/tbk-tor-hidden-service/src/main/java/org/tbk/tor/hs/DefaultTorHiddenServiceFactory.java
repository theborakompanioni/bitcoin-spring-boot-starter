package org.tbk.tor.hs;

import org.berndpruenster.netlayer.tor.HiddenServiceSocket;
import reactor.core.publisher.Mono;

public class DefaultTorHiddenServiceFactory implements TorHiddenServiceFactory {

    @Override
    public HiddenServiceSocket create(HiddenServiceCreateContext context) {
        return new HiddenServiceSocket(
                context.getInternalPort(),
                context.getHiddenServiceDir(),
                context.getHiddenServicePort(),
                context.getTor().orElse(null)
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
