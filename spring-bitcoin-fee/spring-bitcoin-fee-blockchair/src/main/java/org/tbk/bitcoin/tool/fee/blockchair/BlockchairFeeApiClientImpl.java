package org.tbk.bitcoin.tool.fee.blockchair;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.tbk.bitcoin.tool.fee.util.MoreHttpClient;
import org.tbk.bitcoin.tool.fee.util.MoreJsonFormat;

import static java.util.Objects.requireNonNull;

public class BlockchairFeeApiClientImpl implements BlockchairFeeApiClient {
    private final CloseableHttpClient client = HttpClients.createDefault();

    private final String baseUrl;
    private final String apiToken;

    public BlockchairFeeApiClientImpl(String baseUrl, String apiToken) {
        this.baseUrl = requireNonNull(baseUrl);
        this.apiToken = requireNonNull(apiToken);
    }

    @Override
    public BitcoinStatsFeesOnly bitcoinStatsFeesOnly() {
        // https://api.blockchair.com/bitcoin/stats
        String url = String.format("%s/%s", baseUrl, "bitcoin/stats");
        HttpGet request = new HttpGet(url);
        String json = MoreHttpClient.executeToJson(client, request);
        return MoreJsonFormat.jsonToProto(json, BitcoinStatsFeesOnly.newBuilder()).build();
    }
}
