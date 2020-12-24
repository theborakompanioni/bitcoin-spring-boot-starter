package org.tbk.electrum.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

@Value
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SimpleSemanticVersion implements SemanticVersion {
    public static Optional<SemanticVersion> tryParse(String version) {
        requireNonNull(version);

        Pattern compile = Pattern.compile("^(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?.*$");

        Matcher matcher = compile.matcher(version);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        SimpleSemanticVersionBuilder builder = SimpleSemanticVersion.builder();
        if (matcher.groupCount() > 0) {
            builder.major(Integer.parseInt(matcher.group(1)));
        }
        if (matcher.groupCount() > 1) {
            builder.minor(Optional.ofNullable(matcher.group(2))
                    .map(Integer::parseInt)
                    .orElse(0));
        }
        if (matcher.groupCount() > 2) {
            builder.patch(Optional.ofNullable(matcher.group(3))
                    .map(Integer::parseInt)
                    .orElse(0));
        }

        return Optional.of(builder.build());
    }

    int major;
    int minor;
    int patch;
}
