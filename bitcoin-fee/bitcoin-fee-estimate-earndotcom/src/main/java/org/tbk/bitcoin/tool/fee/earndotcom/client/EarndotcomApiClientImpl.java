package org.tbk.bitcoin.tool.fee.earndotcom.client;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.SneakyThrows;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.tbk.bitcoin.tool.fee.earndotcom.client.proto.RecommendedTransactionFees;
import org.tbk.bitcoin.tool.fee.earndotcom.client.proto.TransactionFeesSummary;
import org.tbk.bitcoin.tool.fee.util.MoreHttpClient;
import org.tbk.bitcoin.tool.fee.util.MoreJsonFormat;

import java.net.URI;
import java.net.URISyntaxException;

import static java.util.Objects.requireNonNull;

public class EarndotcomApiClientImpl implements EarndotcomApiClient {
    private final CloseableHttpClient client = HttpClients.createDefault();

    private final String baseUrl;
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private final String apiToken;

    public EarndotcomApiClientImpl(String baseUrl, String apiToken) {
        this.baseUrl = requireNonNull(baseUrl);
        this.apiToken = apiToken;
    }

    @Override
    @SneakyThrows(URISyntaxException.class)
    public RecommendedTransactionFees recommendedTransactionFees() {
        // https://bitcoinfees.earn.com/api/v1/fees/recommended
        URI url = new URIBuilder(baseUrl)
                .setPath("api/v1/fees/recommended")
                .build();

        HttpGet request = new HttpGet(url);
        String json = MoreHttpClient.executeToJson(client, request);
        return MoreJsonFormat.jsonToProto(json, RecommendedTransactionFees.newBuilder()).build();
    }

    @Override
    @SneakyThrows(URISyntaxException.class)
    public TransactionFeesSummary transactionFeesSummary() {
        // https://bitcoinfees.earn.com/api/v1/fees/list
        URI url = new URIBuilder(baseUrl)
                .setPath("api/v1/fees/list")
                .build();

        HttpGet request = new HttpGet(url);
        String json = MoreHttpClient.executeToJson(client, request);
        return MoreJsonFormat.jsonToProto(json, TransactionFeesSummary.newBuilder()).build();
    }
}
