package org.tbk.bitcoin.tool.fee.strike;

import com.google.common.net.HttpHeaders;
import lombok.SneakyThrows;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.tbk.bitcoin.tool.fee.strike.proto.BlendedFeeEstimateResponse;
import org.tbk.bitcoin.tool.fee.util.MoreHttpClient;
import org.tbk.bitcoin.tool.fee.util.MoreJsonFormat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class StrikeFeeApiClientImpl implements StrikeFeeApiClient {
    private static final String DEFAULT_VERSION = Optional.ofNullable(StrikeFeeApiClientImpl.class
            .getPackage()
            .getImplementationVersion()
    ).orElse("0.0.0");

    private static final String DEFAULT_USERAGENT = "tbk-strike-client/" + DEFAULT_VERSION;

    private final CloseableHttpClient client = HttpClients.createDefault();

    private final String baseUrl;

    public StrikeFeeApiClientImpl(String baseUrl) {
        this.baseUrl = requireNonNull(baseUrl);
    }

    @Override
    @SneakyThrows(URISyntaxException.class)
    public BlendedFeeEstimateResponse feeEstimates() {
        // https://bitcoinchainfees.strike.me/v1/fee-estimates
        URI url = new URIBuilder(baseUrl)
                .setPath("v1/fee-estimates")
                .build();

        HttpGet httpRequest = new HttpGet(url);
        httpRequest.addHeader(HttpHeaders.USER_AGENT, DEFAULT_USERAGENT);
        String json = MoreHttpClient.executeToJson(client, httpRequest);
        return MoreJsonFormat.jsonToProto(json, BlendedFeeEstimateResponse.newBuilder()).build();
    }
}
