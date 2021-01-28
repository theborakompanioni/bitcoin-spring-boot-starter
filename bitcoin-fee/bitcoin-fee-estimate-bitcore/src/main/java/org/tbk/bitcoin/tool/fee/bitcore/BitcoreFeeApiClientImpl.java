package org.tbk.bitcoin.tool.fee.bitcore;

import com.google.common.collect.ImmutableMap;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.tbk.bitcoin.tool.fee.util.MoreHttpClient;
import org.tbk.bitcoin.tool.fee.util.MoreJsonFormat;
import org.tbk.bitcoin.tool.fee.util.MoreQueryString;

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

    private Map<String, String> createDefaultParamMap() {
        ImmutableMap.Builder<String, String> queryParamsBuilder = ImmutableMap.builder();
        getApiToken().ifPresent(val -> queryParamsBuilder.put(TOKEN_PARAM_NAME, val));
        return queryParamsBuilder.build();
    }

    @Override
    public FeeEstimationResponse bitcoinMainnetFee(FeeEstimationRequest feeEstimationRequest) {
        checkArgument(feeEstimationRequest.getBlocks() > 0L, "'blocks' must be between 1 and 100");
        checkArgument(feeEstimationRequest.getBlocks() <= 100L, "'blocks' must be between 1 and 100");

        // https://api.bitcore.io/api/BTC/mainnet/fee/1
        String query = MoreQueryString.toQueryString(createDefaultParamMap());
        String url = String.format("%s/%s/%d%s", baseUrl, "api/BTC/mainnet/fee", feeEstimationRequest.getBlocks(), query);
        HttpGet request = new HttpGet(url);
        String json = MoreHttpClient.executeToJson(client, request);
        return MoreJsonFormat.jsonToProto(json, FeeEstimationResponse.newBuilder()).build();
    }
}
