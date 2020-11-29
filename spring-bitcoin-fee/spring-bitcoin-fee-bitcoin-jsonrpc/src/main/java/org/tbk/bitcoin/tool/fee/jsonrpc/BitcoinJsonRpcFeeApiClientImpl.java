package org.tbk.bitcoin.tool.fee.jsonrpc;

import com.google.common.collect.ImmutableList;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class BitcoinJsonRpcFeeApiClientImpl implements BitcoinJsonRpcFeeApiClient {
    private final BitcoinClient client;

    public BitcoinJsonRpcFeeApiClientImpl(BitcoinClient client) {
        this.client = requireNonNull(client);
    }

    @Override
    public EstimateSmartFeeResponse estimatesmartfee(EstimateSmartFeeRequest request) {
        try {
            Map<String, Object> estimatesmartfee = this.client.send("estimatesmartfee", ImmutableList.builder()
                    .add(request.getConfTarget())
                    .build());

            // "errors", when present, can be safely casted to List<String>
            @SuppressWarnings("unchecked")
            List<String> errors = Optional.ofNullable(estimatesmartfee.get("errors"))
                    .map(val -> (List<String>) val)
                    .orElseGet(Collections::emptyList);

            return EstimateSmartFeeResponse.newBuilder()
                    .setBlocks(Optional.ofNullable((Integer) estimatesmartfee.get("blocks")).orElse(0))
                    .setFeerate(Optional.ofNullable((Double) estimatesmartfee.get("feerate")).orElse(0d))
                    .addAllError(errors)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
