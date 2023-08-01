package org.tbk.lightning.regtest.example.api;

import fr.acinq.lightning.MilliSatoshi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.tbk.lightning.cln.grpc.client.*;
import org.tbk.lightning.regtest.core.MoreMilliSatoshi;

import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

import static java.util.Objects.requireNonNull;
import static org.tbk.lightning.regtest.core.LightningNetworkConstants.CLN_DEFAULT_INVOICE_EXPIRY;
import static org.tbk.lightning.regtest.core.LightningNetworkConstants.LARGEST_CHANNEL_SIZE_MSAT;

@Tags({
        @Tag(name = "regtest-node")
})
// hint: must be public in order for the controllers to show up in swagger-ui!
public abstract class AbstractClnNodeApi {

    private final NodeGrpc.NodeBlockingStub node;

    public AbstractClnNodeApi(NodeGrpc.NodeBlockingStub node) {
        this.node = requireNonNull(node);
    }

    @Operation(
            summary = "Create an invoice on user node"
    )
    @PostMapping("/invoice")
    public ResponseEntity<CreateInvoiceResponse> invoice(@RequestParam("millisats") long millisats,
                                                         @RequestParam(name = "expiryInSeconds", required = false) Long expiryInSecondsOrNull) {
        Duration expiry = expiryInSecondsOrNull != null ? Duration.ofSeconds(expiryInSecondsOrNull) : CLN_DEFAULT_INVOICE_EXPIRY;

        InvoiceResponse invoice = node.invoice(InvoiceRequest.newBuilder()
                .setLabel(RandomStringUtils.randomAlphanumeric(32))
                .setAmountMsat(AmountOrAny.newBuilder()
                        .setAmount(Amount.newBuilder()
                                .setMsat(millisats)
                                .build())
                        .build())
                .setExpiry(expiry.toSeconds())
                .build());

        return ResponseEntity.ok(CreateInvoiceResponse.builder()
                .bolt11(invoice.getBolt11())
                .paymentHash(HexFormat.of().formatHex(invoice.getPaymentHash().toByteArray()))
                .expiresAt(Instant.ofEpochSecond(invoice.getExpiresAt()))
                .amountMsat(millisats)
                .build());
    }

    @Operation(
            summary = "Create an invoice on user node with an 'non-payable' amount in the local regtest setup"
    )
    @PostMapping("/non-payable-invoice")
    public ResponseEntity<CreateInvoiceResponse> nonPayableInvoice(@RequestParam(name = "expiryInSeconds", required = false) Long expiryInSecondsOrNull) {
        return invoice(LARGEST_CHANNEL_SIZE_MSAT.plus(new MilliSatoshi(1)).getMsat(), expiryInSecondsOrNull);
    }

    @Operation(
            summary = "Get balance information on user node"
    )
    @GetMapping("/balance-info")
    public ResponseEntity<BalanceInfo> balanceInfo() {
        ListfundsResponse listfundsResponse = node.listFunds(ListfundsRequest.newBuilder().build());

        MilliSatoshi totalCapacity = listfundsResponse.getChannelsList().stream()
                .filter(ListfundsChannels::hasAmountMsat)
                .map(ListfundsChannels::getAmountMsat)
                .map(it -> new MilliSatoshi(it.getMsat()))
                .reduce(MoreMilliSatoshi.ZERO, MilliSatoshi::plus);

        // "outbound liquidity": what this node is able to send
        MilliSatoshi outboundLiquidity = listfundsResponse.getChannelsList().stream()
                .filter(ListfundsChannels::hasOurAmountMsat)
                .map(ListfundsChannels::getOurAmountMsat)
                .map(it -> new MilliSatoshi(it.getMsat()))
                .reduce(MoreMilliSatoshi.ZERO, MilliSatoshi::plus);

        // "inbound liquidity": what this node is able to receive
        MilliSatoshi inboundLiquidity = totalCapacity.minus(outboundLiquidity);

        MilliSatoshi onchainFunds = listfundsResponse.getOutputsList().stream()
                .filter(ListfundsOutputs::hasAmountMsat)
                .map(ListfundsOutputs::getAmountMsat)
                .map(it -> new MilliSatoshi(it.getMsat()))
                .reduce(MoreMilliSatoshi.ZERO, MilliSatoshi::plus);

        return ResponseEntity.ok(BalanceInfo.builder()
                .channelCount(listfundsResponse.getChannelsCount())
                .totalCapacityMsat(totalCapacity.getMsat())
                .outboundMsat(outboundLiquidity.getMsat())
                .inboundMsat(inboundLiquidity.getMsat())
                .utxoCount(listfundsResponse.getOutputsCount())
                .onchainMsat(onchainFunds.getMsat())
                .build());
    }

    @Value
    @Builder
    static class CreateInvoiceResponse {
        @NonNull
        String bolt11;

        @NonNull
        String paymentHash;

        @NonNull
        Instant expiresAt;

        @NonNull
        Long amountMsat;
    }


    @Value
    @Builder
    static class BalanceInfo {
        @NonNull
        Integer channelCount;

        @NonNull
        Long totalCapacityMsat;

        @NonNull
        Long inboundMsat;

        @NonNull
        Long outboundMsat;

        @NonNull
        Integer utxoCount;

        @NonNull
        Long onchainMsat;
    }
}
