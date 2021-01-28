package org.tbk.bitcoin.tool.fee.util;

import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilderSpec;

import java.util.Map;

public final class MoreCacheBuilder {
    private static final Joiner.MapJoiner cacheBuilderSpecMapJoiner = Joiner.on(",").withKeyValueSeparator("=");

    private MoreCacheBuilder() {
        throw new UnsupportedOperationException();
    }

    public static CacheBuilderSpec toCacheBuilderSpec(Map<String, String> configValues) {
        return CacheBuilderSpec.parse(cacheBuilderSpecMapJoiner.join(configValues));
    }
}
