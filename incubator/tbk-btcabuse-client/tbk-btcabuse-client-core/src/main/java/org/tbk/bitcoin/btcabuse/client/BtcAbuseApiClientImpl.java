package org.tbk.bitcoin.btcabuse.client;

import com.google.protobuf.util.JsonFormat;
import lombok.SneakyThrows;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.tbk.bitcoin.tool.btcabuse.CheckResponseDto;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;

public class BtcAbuseApiClientImpl implements BtcAbuseApiClient {

    private final CloseableHttpClient client;

    private final URIBuilder baseUrlBuilder;

    public BtcAbuseApiClientImpl(CloseableHttpClient client, String baseUrl, String apiToken) {
        requireNonNull(baseUrl);
        requireNonNull(apiToken);

        this.client = requireNonNull(client);
        this.baseUrlBuilder = toUriBuilder(baseUrl, apiToken);
    }

    @Override
    @SneakyThrows(URISyntaxException.class)
    public CheckResponseDto check(String address) {
        URI url = baseUrlBuilder
                .setPath("api/reports/check")
                .addParameter("address", address)
                .build();

        HttpGet httpRequest = new HttpGet(url);
        try (CloseableHttpResponse response = client.execute(httpRequest)) {
            HttpEntity entity = response.getEntity();
            try (InputStreamReader isr = new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8)) {
                CheckResponseDto.Builder builder = CheckResponseDto.newBuilder();
                JsonFormat.parser().ignoringUnknownFields().merge(isr, builder);
                return builder.build();
            }
        } catch (IOException e) {
            String errorMessage = String.format("Error while %s", httpRequest);
            throw new RuntimeException(errorMessage, e);
        }
    }

    @Override
    @SneakyThrows(URISyntaxException.class)
    public void downloadCsv(DownloadDuration duration, FileOutputStream outputStream) {
        URI url = baseUrlBuilder
                .setPath("api/download/" + duration.getDuration())
                .build();

        HttpGet httpRequest = new HttpGet(url);
        try (CloseableHttpResponse response = client.execute(httpRequest)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                entity.writeTo(outputStream);
            }
        } catch (IOException e) {
            String errorMessage = String.format("Error while %s", httpRequest);
            throw new RuntimeException(errorMessage, e);
        }
    }
    private static URIBuilder toUriBuilder(String baseUrl, String apiToken) {
        try {
            return new URIBuilder(baseUrl)
                    .addParameter("api_token", apiToken);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
