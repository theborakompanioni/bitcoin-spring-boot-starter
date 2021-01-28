package org.tbk.bitcoin.tool.fee.blockchaininfo;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.tbk.bitcoin.tool.fee.util.MoreHttpClient;
import org.tbk.bitcoin.tool.fee.util.MoreJsonFormat;

import static java.util.Objects.requireNonNull;

public class BlockchainInfoFeeApiClientImpl implements BlockchainInfoFeeApiClient {
    private final CloseableHttpClient client = HttpClients.createDefault();

    private final String baseUrl;
    private final String apiToken;

    public BlockchainInfoFeeApiClientImpl(String baseUrl, String apiToken) {
        this.baseUrl = requireNonNull(baseUrl);
        this.apiToken = apiToken;
    }

    @Override
    public MempoolFees mempoolFees() {
        // https://api.blockchain.info/mempool/fees
        String url = String.format("%s/%s", baseUrl, "mempool/fees");
        HttpGet request = new HttpGet(url);
        String json = MoreHttpClient.executeToJson(client, request);
        return MoreJsonFormat.jsonToProto(json, MempoolFees.newBuilder()).build();
    }
}
