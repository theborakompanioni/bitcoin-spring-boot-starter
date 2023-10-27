package org.tbk.bitcoin.tool.fee.bitgo;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.SneakyThrows;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.tbk.bitcoin.tool.fee.bitgo.proto.BtcTxFeeRequest;
import org.tbk.bitcoin.tool.fee.bitgo.proto.BtcTxFeeResponse;
import org.tbk.bitcoin.tool.fee.util.MoreHttpClient;
import org.tbk.bitcoin.tool.fee.util.MoreJsonFormat;
import org.tbk.bitcoin.tool.fee.util.MoreQueryString;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class BitgoFeeApiClientImpl implements BitgoFeeApiClient {
    // api errors when "num blocks" param is lower than this value
    private static final int MIN_NUM_BLOCKS_PARAM_VAL = 2;

    private final CloseableHttpClient client = HttpClients.createDefault();

    private final String baseUrl;
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private final String apiToken;

    public BitgoFeeApiClientImpl(String baseUrl, String apiToken) {
        this.baseUrl = requireNonNull(baseUrl);
        this.apiToken = apiToken;
    }

    @Override
    @SneakyThrows(URISyntaxException.class)
    public BtcTxFeeResponse btcTxFee(BtcTxFeeRequest request) {
        // https://www.bitgo.com/api/v2/btc/tx/fee
        URI url = new URIBuilder(baseUrl)
                .setPath("api/v2/btc/tx/fee")
                .addParameters(MoreQueryString.toParams(createDefaultParams(request)))
                .build();

        HttpGet httpRequest = new HttpGet(url);
        String json = MoreHttpClient.executeToJson(client, httpRequest);
        return MoreJsonFormat.jsonToProto(json, BtcTxFeeResponse.newBuilder()).build();
    }

    private Map<String, String> createDefaultParams(BtcTxFeeRequest request) {
        return ImmutableMap.<String, String>builder()
                .put("numBlocks", Optional.of(request.getNumBlocks())
                        .filter(val -> val >= MIN_NUM_BLOCKS_PARAM_VAL)
                        .map(val -> Long.toString(val, 10))
                        .orElseGet(() -> Long.toString(MIN_NUM_BLOCKS_PARAM_VAL, 10)))
                .build();
    }
}
