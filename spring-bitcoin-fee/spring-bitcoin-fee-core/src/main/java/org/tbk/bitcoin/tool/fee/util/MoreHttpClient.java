package org.tbk.bitcoin.tool.fee.util;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public final class MoreHttpClient {

    private MoreHttpClient() {
        throw new UnsupportedOperationException();
    }

    public static String executeToJson(CloseableHttpClient client, HttpUriRequest request) {
        try (CloseableHttpResponse response = client.execute(request)) {
            HttpEntity entity = response.getEntity();
            try (InputStream is = entity.getContent();
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                 BufferedReader bufferedReader = new BufferedReader(reader)
            ) {
                return bufferedReader.lines()
                        .collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
