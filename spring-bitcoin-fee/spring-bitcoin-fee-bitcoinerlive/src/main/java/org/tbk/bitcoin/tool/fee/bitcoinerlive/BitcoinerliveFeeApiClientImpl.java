package org.tbk.bitcoin.tool.fee.bitcoinerlive;

import com.google.common.collect.ImmutableMap;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.tbk.bitcoin.tool.fee.bitcoinerlive.FeeEstimatesLatestRequest.ConfidenceOrValueCase;
import org.tbk.bitcoin.tool.fee.util.MoreHttpClient;
import org.tbk.bitcoin.tool.fee.util.MoreJsonFormat;
import org.tbk.bitcoin.tool.fee.util.MoreQueryString;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class BitcoinerliveFeeApiClientImpl implements BitcoinerliveFeeApiClient {
    private static final String DEFAULT_VERSION = Optional.ofNullable(BitcoinerliveFeeApiClientImpl.class
            .getPackage()
            .getImplementationVersion()
    ).orElse("0.0.0");

    private static final String DEFAULT_USERAGENT = "tbk-bitcoinerlive-client/" + DEFAULT_VERSION;

    private final static String TOKEN_PARAM_NAME = "token";
    private final CloseableHttpClient client = HttpClients.createDefault();

    private final String baseUrl;
    private final String apiToken;

    private static class ConfidenceConstants {
        static final String LOW = "0.5";
        static final String MEDIUM = "0.8";
        static final String HIGH = "0.9";
    }

    public BitcoinerliveFeeApiClientImpl(String baseUrl, String apiToken) {
        this.baseUrl = requireNonNull(baseUrl);
        this.apiToken = apiToken;
    }

    private Optional<String> getApiToken() {
        return Optional.ofNullable(this.apiToken);
    }

    @Override
    public FeeEstimatesLatestResponse feeEstimatesLatest(FeeEstimatesLatestRequest request) {
        String query = MoreQueryString.toQueryString(ImmutableMap.<String, String>builder()
                .put("confidence", toConfidenceValue(request))
                .build());

        // https://bitcoiner.live/api/fees/estimates/latest
        String url = String.format("%s/%s%s", baseUrl, "api/fees/estimates/latest", query);
        HttpGet httpRequest = new HttpGet(url);
        String json = MoreHttpClient.executeToJson(client, httpRequest);
        return MoreJsonFormat.jsonToProto(json, FeeEstimatesLatestResponse.newBuilder()).build();
    }

    private static String toConfidenceValue(FeeEstimatesLatestRequest request) {
        switch (request.getConfidenceOrValueCase()) {
            case CONFIDENCE_VAL:
                return String.valueOf(request.getConfidenceVal());
            case CONFIDENCE_TYPE:
                switch (request.getConfidenceType()) {
                    case LOW:
                        return ConfidenceConstants.LOW;
                    case MEDIUM:
                        return ConfidenceConstants.MEDIUM;
                    case HIGH:
                    case UNRECOGNIZED:
                    default:
                        return ConfidenceConstants.HIGH;
                }
            case CONFIDENCEORVALUE_NOT_SET:
            default:
                return ConfidenceConstants.HIGH;
        }
    }
}
