package org.tbk.bitcoin.btcabuse.client;

import com.google.protobuf.util.JsonFormat;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.tbk.bitcoin.tool.btcabuse.CheckResponseDto;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.util.Objects.requireNonNull;

public class BtcAbuseApiClientImpl implements BtcAbuseApiClient {

    private final CloseableHttpClient client;

    private final String baseUrl;

    private final String apiToken;

    public BtcAbuseApiClientImpl(CloseableHttpClient client, String baseUrl, String apiToken) {
        this.client = requireNonNull(client);
        this.baseUrl = requireNonNull(baseUrl);
        this.apiToken = requireNonNull(apiToken);
    }

    @Override
    public CheckResponseDto check(String address) {
        String url = String.format("%s/%s?api_token=%s&address=%s", baseUrl, "api/reports/check", apiToken, address);
        HttpGet httpRequest = new HttpGet(url);
        try (CloseableHttpResponse response = client.execute(httpRequest)) {
            HttpEntity entity = response.getEntity();
            try (InputStreamReader isr = new InputStreamReader(entity.getContent())) {
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
    public void downloadCsv(DownloadDuration duration, FileOutputStream outputStream) {
        String url = String.format("%s/%s/%s?api_token=%s", baseUrl, "api/download", duration.getDuration(), apiToken);

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
}
