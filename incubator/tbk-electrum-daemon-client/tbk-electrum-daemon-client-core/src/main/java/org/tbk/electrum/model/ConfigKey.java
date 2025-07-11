package org.tbk.electrum.model;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigKey {
    public static ConfigKey of(String key) {
        return new ConfigKey(key);
    }

    @NonNull
    String key;
}
