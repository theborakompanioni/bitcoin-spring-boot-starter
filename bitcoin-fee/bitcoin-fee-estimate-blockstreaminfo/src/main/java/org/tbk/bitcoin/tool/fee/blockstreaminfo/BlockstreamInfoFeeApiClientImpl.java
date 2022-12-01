package org.tbk.bitcoin.tool.fee.blockstreaminfo;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import lombok.SneakyThrows;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.tbk.bitcoin.tool.fee.util.MoreHttpClient;
import org.tbk.bitcoin.tool.fee.util.MoreJsonFormat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class BlockstreamInfoFeeApiClientImpl implements BlockstreamInfoFeeApiClient {
    private final CloseableHttpClient client = HttpClients.createDefault();

    private final String baseUrl;
    private final String apiToken;

    public BlockstreamInfoFeeApiClientImpl(String baseUrl, String apiToken) {
        this.baseUrl = requireNonNull(baseUrl);
        this.apiToken = apiToken;
    }

    @Override
    @SneakyThrows(URISyntaxException.class)
    public FeeEstimates feeEstimates() {
        // https://blockstream.info/api/fee-estimates
        URI url = new URIBuilder(baseUrl)
                .setPath("api/fee-estimates")
                .build();

        HttpGet request = new HttpGet(url);
        String json = MoreHttpClient.executeToJson(client, request);
        Struct messageAsStruct = MoreJsonFormat.jsonToProto(json, Struct.newBuilder()).build();

        List<FeeEstimates.Entry> entries = messageAsStruct.getFieldsMap().entrySet().stream()
                .filter(val -> val.getValue().getKindCase().equals(Value.KindCase.NUMBER_VALUE))
                .map(val -> FeeEstimates.Entry.newBuilder()
                        .setNumberOfBlocks(Long.parseLong(val.getKey(), 10))
                        .setEstimatedFeerateInSatPerVbyte(val.getValue().getNumberValue())
                        .build())
                .sorted(Comparator.comparingLong(FeeEstimates.Entry::getNumberOfBlocks))
                .toList();

        return FeeEstimates.newBuilder()
                .addAllEntry(entries)
                .build();
    }
}
