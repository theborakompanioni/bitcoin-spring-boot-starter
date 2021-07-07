package org.tbk.bitcoin.tool.fee.util;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public final class MoreHttpClient {

    private MoreHttpClient() {
        throw new UnsupportedOperationException();
    }

    public static String executeToJson(CloseableHttpClient client, HttpUriRequest request) {
        try (CloseableHttpResponse response = client.execute(request)) {
            HttpEntity entity = response.getEntity();
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8))) {
                return bufferedReader.lines().collect(Collectors.joining("\n"));
            } finally {
                EntityUtils.consumeQuietly(entity);
            }
        } catch (IOException e) {
            String errorMessage = String.format("Error while executing request to %s", request.getURI().getHost());
            throw new RuntimeException(errorMessage, e);
        }
    }
}
