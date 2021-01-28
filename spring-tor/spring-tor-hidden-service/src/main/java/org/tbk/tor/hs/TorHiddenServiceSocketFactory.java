package org.tbk.tor.hs;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.berndpruenster.netlayer.tor.HiddenServiceSocket;
import org.berndpruenster.netlayer.tor.Tor;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.Optional;

public interface TorHiddenServiceSocketFactory {

    HiddenServiceSocket create(HiddenServiceSocketCreateContext context);

    Mono<HiddenServiceSocket> createReady(HiddenServiceSocketCreateContext context);

    @Value
    @Builder
    class HiddenServiceSocketCreateContext {
        @NonNull
        Integer hostPort;

        @NonNull
        String hiddenServiceDir;

        @Nullable
        Integer virtualPort;

        @Nullable
        Tor tor;

        public int getVirtualPort() {
            return Optional.ofNullable(virtualPort).orElse(hostPort);
        }

        public Optional<Tor> getTor() {
            return Optional.ofNullable(tor);
        }
    }
}
