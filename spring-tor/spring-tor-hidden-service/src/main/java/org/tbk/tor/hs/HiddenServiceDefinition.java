package org.tbk.tor.hs;

import com.google.common.base.MoreObjects;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import lombok.*;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Value
@Builder
public class HiddenServiceDefinition {

    @Nullable
    String name;

    @NonNull
    Integer virtualPort;

    @NonNull
    String host;

    @NonNull
    Integer port;

    @NonNull
    File directory;

    @Getter(value = AccessLevel.PRIVATE)
    Supplier<String> virtualHostSupplier = Suppliers.memoize(this::getVirtualHostOrThrow);

    public String getName() {
        return Optional.ofNullable(name)
                .orElseGet(directory::getName);
    }

    public int getVirtualPort() {
        return virtualPort;
    }

    public int getPort() {
        return port;
    }

    public Optional<String> getVirtualHost() {
        try {
            return Optional.of(virtualHostSupplier.get());
        } catch (IllegalStateException e) {
            return Optional.empty();
        }
    }

    public Optional<String> getVirtualHostUncached() {
        try {
            return Optional.of(getVirtualHostOrThrow());
        } catch (IllegalStateException e) {
            return Optional.empty();
        }
    }

    public String getVirtualHostOrThrow() throws IllegalStateException {
        try {
            Path hostnamePath = Path.of(directory.getAbsolutePath(), "hostname");

            return Files.readAllLines(hostnamePath).stream().findFirst()
                    .filter(it -> !it.isBlank())
                    .orElseThrow(() -> new IllegalStateException("File is empty: " + hostnamePath));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("virtualHost", getVirtualHost().orElse("unknown"))
                .add("virtualPort", virtualPort)
                .add("host", host)
                .add("port", port)
                .add("directory", directory.getAbsolutePath())
                .toString();
    }
}