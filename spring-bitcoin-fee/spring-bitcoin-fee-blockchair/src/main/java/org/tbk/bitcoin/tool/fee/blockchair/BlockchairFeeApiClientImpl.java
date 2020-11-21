package org.tbk.bitcoin.tool.fee.blockchair;

import com.google.common.collect.ImmutableMap;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.tbk.bitcoin.tool.fee.util.MoreHttpClient;
import org.tbk.bitcoin.tool.fee.util.MoreJsonFormat;
import org.tbk.bitcoin.tool.fee.util.MoreQueryString;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class BlockchairFeeApiClientImpl implements BlockchairFeeApiClient {
    private final CloseableHttpClient client = HttpClients.createDefault();

    private final String baseUrl;
    private final String apiToken;

    public BlockchairFeeApiClientImpl(String baseUrl, String apiToken) {
        this.baseUrl = requireNonNull(baseUrl);
        this.apiToken = apiToken;
    }

    public Optional<String> getApiToken() {
        return Optional.ofNullable(apiToken);
    }

    @Override
    public BitcoinStatsFeesOnly bitcoinStatsFeesOnly() {
        var queryParamBuilder = ImmutableMap.<String, String>builder();
        getApiToken().ifPresent(val -> queryParamBuilder.put("key", val));
        String query = MoreQueryString.toQueryString(queryParamBuilder.build());

        // https://api.blockchair.com/bitcoin/stats
        String url = String.format("%s/%s%s", baseUrl, "bitcoin/stats", query);
        HttpGet request = new HttpGet(url);
        String json = MoreHttpClient.executeToJson(client, request);
        return MoreJsonFormat.jsonToProto(json, BitcoinStatsFeesOnly.newBuilder()).build();
    }
}
