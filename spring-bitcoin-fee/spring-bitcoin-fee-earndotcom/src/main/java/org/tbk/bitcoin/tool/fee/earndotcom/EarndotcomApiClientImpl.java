package org.tbk.bitcoin.tool.fee.earndotcom;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.tbk.bitcoin.tool.fee.util.MoreHttpClient;
import org.tbk.bitcoin.tool.fee.util.MoreJsonFormat;

import static java.util.Objects.requireNonNull;

public class EarndotcomApiClientImpl implements EarndotcomApiClient {
    private final CloseableHttpClient client = HttpClients.createDefault();

    private final String baseUrl;
    private final String apiToken;

    public EarndotcomApiClientImpl(String baseUrl, String apiToken) {
        this.baseUrl = requireNonNull(baseUrl);
        this.apiToken = apiToken;
    }

    @Override
    public RecommendedTransactionFees recommendedTransactionFees() {
        // https://bitcoinfees.earn.com/api/v1/fees/recommended
        String url = String.format("%s/%s", baseUrl, "api/v1/fees/recommended");
        HttpGet request = new HttpGet(url);
        String json = MoreHttpClient.executeToJson(client, request);
        return MoreJsonFormat.jsonToProto(json, RecommendedTransactionFees.newBuilder()).build();
    }

    @Override
    public TransactionFeesSummary transactionFeesSummary() {
        // https://bitcoinfees.earn.com/api/v1/fees/list
        String url = String.format("%s/%s", baseUrl, "api/v1/fees/list");
        HttpGet request = new HttpGet(url);
        String json = MoreHttpClient.executeToJson(client, request);
        return MoreJsonFormat.jsonToProto(json, TransactionFeesSummary.newBuilder()).build();
    }
}
