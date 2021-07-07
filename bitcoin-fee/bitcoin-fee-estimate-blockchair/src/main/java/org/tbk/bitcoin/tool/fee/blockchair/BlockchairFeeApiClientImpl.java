package org.tbk.bitcoin.tool.fee.blockchair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.tbk.bitcoin.tool.fee.util.MoreHttpClient;
import org.tbk.bitcoin.tool.fee.util.MoreJsonFormat;
import org.tbk.bitcoin.tool.fee.util.MoreQueryString;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class BlockchairFeeApiClientImpl implements BlockchairFeeApiClient {
    private final static String TOKEN_PARAM_NAME = "key";
    private final CloseableHttpClient client = HttpClients.createDefault();

    private final String baseUrl;
    private final String apiToken;

    public BlockchairFeeApiClientImpl(String baseUrl, String apiToken) {
        this.baseUrl = requireNonNull(baseUrl);
        this.apiToken = apiToken;
    }

    private Optional<String> getApiToken() {
        return Optional.ofNullable(apiToken);
    }

    @Override
    @SneakyThrows(URISyntaxException.class)
    public BitcoinStatsFeesOnly bitcoinStatsFeesOnly() {
        // https://api.blockchair.com/bitcoin/stats
        URI url = new URIBuilder(baseUrl)
                .setPath("bitcoin/stats")
                .addParameters(MoreQueryString.toParams(createDefaultParams()))
                .build();

        HttpGet request = new HttpGet(url);
        String json = MoreHttpClient.executeToJson(client, request);
        return MoreJsonFormat.jsonToProto(json, BitcoinStatsFeesOnly.newBuilder()).build();
    }

    private Map<String, String> createDefaultParams() {
        var queryParamBuilder = ImmutableMap.<String, String>builder();
        getApiToken().ifPresent(val -> queryParamBuilder.put(TOKEN_PARAM_NAME, val));
        return queryParamBuilder.build();
    }

}
