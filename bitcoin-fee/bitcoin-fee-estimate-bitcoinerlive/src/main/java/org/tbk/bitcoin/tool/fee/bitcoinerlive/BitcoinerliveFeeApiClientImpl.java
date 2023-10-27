package org.tbk.bitcoin.tool.fee.bitcoinerlive;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.HttpHeaders;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.SneakyThrows;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.tbk.bitcoin.tool.fee.bitcoinerlive.proto.FeeEstimatesLatestRequest;
import org.tbk.bitcoin.tool.fee.bitcoinerlive.proto.FeeEstimatesLatestResponse;
import org.tbk.bitcoin.tool.fee.util.MoreHttpClient;
import org.tbk.bitcoin.tool.fee.util.MoreJsonFormat;
import org.tbk.bitcoin.tool.fee.util.MoreQueryString;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class BitcoinerliveFeeApiClientImpl implements BitcoinerliveFeeApiClient {
    private static final String DEFAULT_VERSION = Optional.ofNullable(BitcoinerliveFeeApiClientImpl.class
            .getPackage()
            .getImplementationVersion()
    ).orElse("0.0.0");

    private static final String DEFAULT_USERAGENT = "tbk-bitcoinerlive-client/" + DEFAULT_VERSION;

    private final CloseableHttpClient client = HttpClients.createDefault();

    private final String baseUrl;
    @SuppressFBWarnings("URF_UNREAD_FIELD")
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

    @Override
    @SneakyThrows(URISyntaxException.class)
    public FeeEstimatesLatestResponse feeEstimatesLatest(FeeEstimatesLatestRequest request) {
        // https://bitcoiner.live/api/fees/estimates/latest
        URI url = new URIBuilder(baseUrl)
                .setPath("api/fees/estimates/latest")
                .addParameters(MoreQueryString.toParams(createDefaultParams(request)))
                .build();

        HttpGet httpRequest = new HttpGet(url);
        httpRequest.addHeader(HttpHeaders.USER_AGENT, DEFAULT_USERAGENT);
        String json = MoreHttpClient.executeToJson(client, httpRequest);
        return MoreJsonFormat.jsonToProto(json, FeeEstimatesLatestResponse.newBuilder()).build();
    }

    private static String toConfidenceValue(FeeEstimatesLatestRequest request) {
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
    }

    private Map<String, String> createDefaultParams(FeeEstimatesLatestRequest request) {
        ImmutableMap.Builder<String, String> params = ImmutableMap.<String, String>builder()
                .put("confidence", toConfidenceValue(request));
        return params.build();
    }
}
