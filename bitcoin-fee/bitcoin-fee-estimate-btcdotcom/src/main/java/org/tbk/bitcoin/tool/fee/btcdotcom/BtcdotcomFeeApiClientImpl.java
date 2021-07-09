package org.tbk.bitcoin.tool.fee.btcdotcom;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.HttpHeaders;
import lombok.SneakyThrows;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.tbk.bitcoin.tool.fee.util.MoreHttpClient;
import org.tbk.bitcoin.tool.fee.util.MoreJsonFormat;
import org.tbk.bitcoin.tool.fee.util.MoreQueryString;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class BtcdotcomFeeApiClientImpl implements BtcdotcomFeeApiClient {
    private static final String DEFAULT_VERSION = Optional.ofNullable(BtcdotcomFeeApiClientImpl.class
            .getPackage()
            .getImplementationVersion()
    ).orElse("0.0.0");

    private static final String DEFAULT_USERAGENT = "tbk-btcdotcom-client/" + DEFAULT_VERSION;

    private static final String TOKEN_PARAM_NAME = "token";
    private final CloseableHttpClient client = HttpClients.createDefault();

    private final String baseUrl;
    private final String apiToken;

    public BtcdotcomFeeApiClientImpl(String baseUrl, String apiToken) {
        this.baseUrl = requireNonNull(baseUrl);
        this.apiToken = apiToken;
    }

    private Optional<String> getApiToken() {
        return Optional.ofNullable(this.apiToken);
    }

    private Map<String, String> createDefaultParams() {
        ImmutableMap.Builder<String, String> queryParamsBuilder = ImmutableMap.builder();
        getApiToken().ifPresent(val -> queryParamsBuilder.put(TOKEN_PARAM_NAME, val));
        return queryParamsBuilder.build();
    }

    @Override
    @SneakyThrows(URISyntaxException.class)
    public FeeDistribution feeDistribution() {
        // https://btc.com/service/fees/distribution
        URI url = new URIBuilder(baseUrl)
                .setPath("service/fees/distribution")
                .addParameters(MoreQueryString.toParams(createDefaultParams()))
                .build();

        HttpGet request = new HttpGet(url);
        request.addHeader(HttpHeaders.USER_AGENT, DEFAULT_USERAGENT);
        String json = MoreHttpClient.executeToJson(client, request);
        return MoreJsonFormat.jsonToProto(json, FeeDistribution.newBuilder()).build();
    }
}
