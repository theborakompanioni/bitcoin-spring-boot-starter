package org.tbk.electrum.model;

import lombok.*;

import java.util.Optional;

@Value
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SimpleVersion implements Version {
    public static SimpleVersion from(String version) {
        return SimpleVersion.builder()
                .version(version)
                .semanticVersion(SimpleSemanticVersion.tryParse(version).orElse(null))
                .build();
    }

    @NonNull
    String version;

    SemanticVersion semanticVersion;

    public Optional<SemanticVersion> getSemanticVersion() {
        return Optional.ofNullable(this.semanticVersion);
    }
}
