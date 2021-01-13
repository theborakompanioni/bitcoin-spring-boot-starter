package org.tbk.tor.hs;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.berndpruenster.netlayer.tor.HiddenServiceSocket;
import org.berndpruenster.netlayer.tor.Tor;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.Optional;

public interface TorHiddenServiceFactory {

    HiddenServiceSocket create(HiddenServiceCreateContext context);

    Mono<HiddenServiceSocket> createReady(HiddenServiceCreateContext context);

    @Value
    @Builder
    class HiddenServiceCreateContext {
        @NonNull
        Integer internalPort;

        @NonNull
        String hiddenServiceDir;

        @Nullable
        Integer hiddenServicePort;

        @Nullable
        Tor tor;

        public int getHiddenServicePort() {
            return Optional.ofNullable(hiddenServicePort).orElse(internalPort);
        }

        public Optional<Tor> getTor() {
            return Optional.ofNullable(tor);
        }
    }
}
