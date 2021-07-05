package org.tbk.bitcoin.tool.fee.blockcypher;

import com.google.common.collect.ImmutableList;
import lombok.SneakyThrows;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.tbk.bitcoin.tool.fee.util.MoreHttpClient;
import org.tbk.bitcoin.tool.fee.util.MoreJsonFormat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
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

    private List<NameValuePair> createDefaultParams() {
        ImmutableList.Builder<NameValuePair> queryParamsBuilder = ImmutableList.builder();
        getApiToken()
                .map(token -> new BasicNameValuePair(TOKEN_PARAM_NAME, token))
                .ifPresent(queryParamsBuilder::add);
        return queryParamsBuilder.build();
    }

    @Override
    @SneakyThrows(URISyntaxException.class)
    public ChainInfo btcMain() {
        // https://api.blockcypher.com/v1/btc/main
        URI url = new URIBuilder(baseUrl)
                .setPath("v1/btc/main")
                .addParameters(createDefaultParams())
                .build();
        HttpGet request = new HttpGet(url);
        String json = MoreHttpClient.executeToJson(client, request);
        return MoreJsonFormat.jsonToProto(json, ChainInfo.newBuilder()).build();
    }

    @Override
    @SneakyThrows(URISyntaxException.class)
    public ChainInfo btcTestnet3() {
        // https://api.blockcypher.com/v1/btc/test3
        URI url = new URIBuilder(baseUrl)
                .setPath("v1/btc/test3")
                .addParameters(createDefaultParams())
                .build();
        HttpGet request = new HttpGet(url);
        String json = MoreHttpClient.executeToJson(client, request);
        return MoreJsonFormat.jsonToProto(json, ChainInfo.newBuilder()).build();
    }
}
