package org.tbk.bitcoin.tool.fee.util;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MoreQueryString {

    private MoreQueryString() {
        throw new UnsupportedOperationException();
    }

    /**
     * Simple {@link NameValuePair} list from a map. Does not support multi values.
     *
     * @param queryParams map of key -> value
     * @return a list to be used as query in an {@link org.apache.http.client.utils.URIBuilder}
     */
    public static List<NameValuePair> toParams(Map<String, String> queryParams) {
        return queryParams.entrySet().stream()
                .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                .collect(Collectors.toUnmodifiableList());
    }
}
