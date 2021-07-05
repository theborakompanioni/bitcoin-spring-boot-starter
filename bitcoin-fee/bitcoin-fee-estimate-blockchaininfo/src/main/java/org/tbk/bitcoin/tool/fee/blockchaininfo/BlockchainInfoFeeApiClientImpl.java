package org.tbk.bitcoin.tool.fee.blockchaininfo;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.tbk.bitcoin.tool.fee.util.MoreHttpClient;
import org.tbk.bitcoin.tool.fee.util.MoreJsonFormat;

import java.net.URI;
import java.net.URISyntaxException;

import static java.util.Objects.requireNonNull;

public class BlockchainInfoFeeApiClientImpl implements BlockchainInfoFeeApiClient {
    private final CloseableHttpClient client = HttpClients.createDefault();

    private final URI url;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private final String apiToken;

    public BlockchainInfoFeeApiClientImpl(String baseUrl, String apiToken) {
        requireNonNull(baseUrl);
        this.url = toUri(baseUrl);
        this.apiToken = apiToken;
    }

    @Override
    public MempoolFees mempoolFees() {
        HttpGet request = new HttpGet(url);
        String json = MoreHttpClient.executeToJson(client, request);
        return MoreJsonFormat.jsonToProto(json, MempoolFees.newBuilder()).build();
    }

    private static URI toUri(String baseUrl) {
        try {
            // https://api.blockchain.info/mempool/fees
            return new URIBuilder(baseUrl).setPath("mempool/fees").build();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
