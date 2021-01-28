package org.tbk.bitcoin.tool.fee.blockcypher;

import com.google.common.collect.ImmutableMap;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.tbk.bitcoin.tool.fee.util.MoreHttpClient;
import org.tbk.bitcoin.tool.fee.util.MoreJsonFormat;
import org.tbk.bitcoin.tool.fee.util.MoreQueryString;

import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class BlockcypherFeeApiClientImpl implements BlockcypherFeeApiClient {
    private final static String TOKEN_PARAM_NAME = "token";
    private final CloseableHttpClient client = HttpClients.createDefault();

    private final String baseUrl;
    private final String apiToken;

    public BlockcypherFeeApiClientImpl(String baseUrl, String apiToken) {
        this.baseUrl = requireNonNull(baseUrl);
        this.apiToken = apiToken;
    }

    private Optional<String> getApiToken() {
        return Optional.ofNullable(this.apiToken);
    }

    private Map<String, String> createDefaultParamMap() {
        ImmutableMap.Builder<String, String> queryParamsBuilder = ImmutableMap.builder();
        getApiToken().ifPresent(val -> queryParamsBuilder.put(TOKEN_PARAM_NAME, val));
        return queryParamsBuilder.build();
    }

    @Override
    public ChainInfo btcMain() {
        // https://api.blockcypher.com/v1/btc/main
        String query = MoreQueryString.toQueryString(createDefaultParamMap());
        String url = String.format("%s/%s%s", baseUrl, "v1/btc/main", query);
        HttpGet request = new HttpGet(url);
        String json = MoreHttpClient.executeToJson(client, request);
        return MoreJsonFormat.jsonToProto(json, ChainInfo.newBuilder()).build();
    }

    @Override
    public ChainInfo btcTestnet3() {
        // https://api.blockcypher.com/v1/btc/test3
        String query = MoreQueryString.toQueryString(createDefaultParamMap());
        String url = String.format("%s/%s%s", baseUrl, "v1/btc/test3", query);
        HttpGet request = new HttpGet(url);
        String json = MoreHttpClient.executeToJson(client, request);
        return MoreJsonFormat.jsonToProto(json, ChainInfo.newBuilder()).build();
    }
}
