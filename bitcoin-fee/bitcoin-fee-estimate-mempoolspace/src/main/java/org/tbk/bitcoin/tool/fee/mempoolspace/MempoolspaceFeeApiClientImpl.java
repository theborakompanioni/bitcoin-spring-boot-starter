package org.tbk.bitcoin.tool.fee.mempoolspace;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.HttpHeaders;
import com.google.protobuf.ListValue;
import com.google.protobuf.Value;
import lombok.SneakyThrows;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.tbk.bitcoin.tool.fee.mempoolspace.ProjectedMempoolBlocks.ProjectedBlock;
import org.tbk.bitcoin.tool.fee.util.MoreHttpClient;
import org.tbk.bitcoin.tool.fee.util.MoreJsonFormat;
import org.tbk.bitcoin.tool.fee.util.MoreQueryString;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class MempoolspaceFeeApiClientImpl implements MempoolspaceFeeApiClient {
    private static final String DEFAULT_VERSION = Optional.ofNullable(MempoolspaceFeeApiClientImpl.class
            .getPackage()
            .getImplementationVersion()
    ).orElse("0.0.0");

    private static final String DEFAULT_USERAGENT = "tbk-mempoolspace-client/" + DEFAULT_VERSION;

    private final static String TOKEN_PARAM_NAME = "token";
    private final CloseableHttpClient client = HttpClients.createDefault();

    private final String baseUrl;
    private final String apiToken;

    public MempoolspaceFeeApiClientImpl(String baseUrl, String apiToken) {
        this.baseUrl = requireNonNull(baseUrl);
        this.apiToken = apiToken;
    }

    @Override
    @SneakyThrows(URISyntaxException.class)
    public FeesRecommended feesRecommended() {
        // https://mempool.space/api/v1/fees/recommended
        URI url = new URIBuilder(baseUrl)
                .setPath("api/v1/fees/recommended")
                .addParameters(MoreQueryString.toParams(createDefaultParamMap()))
                .build();

        HttpGet request = new HttpGet(url);
        request.addHeader(HttpHeaders.USER_AGENT, DEFAULT_USERAGENT);
        String json = MoreHttpClient.executeToJson(client, request);
        return MoreJsonFormat.jsonToProto(json, FeesRecommended.newBuilder()).build();
    }

    @Override
    @SneakyThrows(URISyntaxException.class)
    public ProjectedMempoolBlocks projectedBlocks() {
        // https://mempool.space/api/v1/fees/mempool-blocks
        URI url = new URIBuilder(baseUrl)
                .setPath("api/v1/fees/mempool-blocks")
                .addParameters(MoreQueryString.toParams(createDefaultParamMap()))
                .build();

        HttpGet request = new HttpGet(url);
        request.addHeader(HttpHeaders.USER_AGENT, DEFAULT_USERAGENT);
        String json = MoreHttpClient.executeToJson(client, request);

        ListValue messageAsListValue = MoreJsonFormat.jsonToProto(json, ListValue.newBuilder()).build();

        List<ProjectedBlock> projectedBlocks = messageAsListValue.getValuesList().stream()
                .filter(it -> it.getKindCase().equals(Value.KindCase.STRUCT_VALUE))
                .map(Value::getStructValue)
                .map(it -> ProjectedBlock.newBuilder()
                        .setBlockSize((long) it.getFieldsOrThrow("blockSize").getNumberValue())
                        .setBlockVsize((long) it.getFieldsOrThrow("blockVSize").getNumberValue())
                        .setNTx((long) it.getFieldsOrThrow("nTx").getNumberValue())
                        .setTotalFees((long) it.getFieldsOrThrow("totalFees").getNumberValue())
                        .setMedianFee(it.getFieldsOrThrow("medianFee").getNumberValue())
                        .addAllFeeRange(it.getFieldsOrThrow("feeRange").getListValue().getValuesList().stream()
                                .map(Value::getNumberValue)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return ProjectedMempoolBlocks.newBuilder()
                .addAllBlocks(projectedBlocks)
                .build();
    }

    private Optional<String> getApiToken() {
        return Optional.ofNullable(this.apiToken);
    }

    private Map<String, String> createDefaultParamMap() {
        ImmutableMap.Builder<String, String> queryParamsBuilder = ImmutableMap.builder();
        getApiToken().ifPresent(val -> queryParamsBuilder.put(TOKEN_PARAM_NAME, val));
        return queryParamsBuilder.build();
    }

}
