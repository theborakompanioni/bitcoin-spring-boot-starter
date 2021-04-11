package org.tbk.bitcoin.tool.fee.util;

import java.util.Map;
import java.util.stream.Collectors;

public final class MoreQueryString {

    private MoreQueryString() {
        throw new UnsupportedOperationException();
    }

    /**
     * Simple query string from map. Does not support multi values.
     * <p>
     * in: { "key" -> "value", "key2" -> "value2" }
     * out: "?key=value&key2=value2
     * <p>
     * If map is empty, just "?" will be returned.
     * in: { }
     * out: "?"
     * <p>
     *
     * @param queryParams map of key -> value
     * @return string to be used as query in an url
     */
    public static String toQueryString(Map<String, String> queryParams) {
        // todo: should be escaped
        return queryParams.entrySet().stream()
                .map(val -> val.getKey() + "=" + val.getValue())
                .collect(Collectors.joining("&", "?", ""));
    }
}
