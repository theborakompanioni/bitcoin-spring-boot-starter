package org.tbk.bitcoin.tool.fee.bitcore;

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

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class BitcoreFeeApiClientImpl implements BitcoreFeeApiClient {
    private final static String TOKEN_PARAM_NAME = "token";
    private final CloseableHttpClient client = HttpClients.createDefault();

    private final String baseUrl;
    private final String apiToken;

    public BitcoreFeeApiClientImpl(String baseUrl, String apiToken) {
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
    public FeeEstimationResponse bitcoinMainnetFee(FeeEstimationRequest feeEstimationRequest) {
        checkArgument(feeEstimationRequest.getBlocks() > 0L, "'blocks' must be between 1 and 100");
        checkArgument(feeEstimationRequest.getBlocks() <= 100L, "'blocks' must be between 1 and 100");

        // https://api.bitcore.io/api/BTC/mainnet/fee/1
        URI url = new URIBuilder(baseUrl)
                .setPath("api/BTC/mainnet/fee/" + feeEstimationRequest.getBlocks())
                .addParameters(createDefaultParams())
                .build();

        HttpGet request = new HttpGet(url);
        String json = MoreHttpClient.executeToJson(client, request);
        return MoreJsonFormat.jsonToProto(json, FeeEstimationResponse.newBuilder()).build();
    }
}
