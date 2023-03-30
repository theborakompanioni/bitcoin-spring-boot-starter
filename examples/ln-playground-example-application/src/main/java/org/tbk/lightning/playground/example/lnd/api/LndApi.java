package org.tbk.lightning.playground.example.lnd.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lightningj.lnd.proto.LightningApi;
import org.lightningj.lnd.wrapper.Message;
import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.ValidationException;
import org.lightningj.lnd.wrapper.message.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tbk.lightning.playground.example.api.dto.CreateInvoiceResponseDto;
import org.tbk.lightning.playground.example.api.dto.NodeInfoDto;
import org.testcontainers.shaded.com.google.common.primitives.Longs;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/lnd", produces = "application/json")
@RequiredArgsConstructor
@Tags({
        @Tag(name = "lnd")
})
public class LndApi {

    @NonNull
    private final SynchronousLndAPI lndApi;

    @NonNull
    private final ObjectMapper objectMapper;

    @Operation(
            summary = "Fetch node info"
    )
    @GetMapping(value = "/info")
    public ResponseEntity<NodeInfoDto> getInfo() throws StatusException, ValidationException {
        GetInfoResponse info = lndApi.getInfo();
        return ResponseEntity.ok(NodeInfoDto.builder()
                .id(info.getIdentityPubkey())
                .alias(info.getAlias())
                .version(info.getVersion())
                .raw(toJson(info))
                .build());
    }

    @Operation(
            summary = "Create invoice"
    )
    @PostMapping(value = "/invoice")
    public ResponseEntity<CreateInvoiceResponseDto> addInvoice(@RequestBody Map<String, Object> body) throws StatusException, ValidationException {

        String memo = Optional.ofNullable(body.get("memo"))
                .map(Object::toString)
                .orElse("");

        long msats = Optional.ofNullable(body.get("msats"))
                .map(Object::toString)
                .map(it -> Longs.tryParse(it, 10))
                .filter(it -> it > 0)
                .orElseThrow(() -> new IllegalArgumentException("'value' must be a positive integer"));

        LightningApi.Invoice invoice = LightningApi.Invoice.newBuilder()
                .setValueMsat(msats)
                .setMemo(memo)
                .build();

        AddInvoiceResponse addInvoiceResponse = lndApi.addInvoice(new Invoice(invoice));

        return ResponseEntity.ok(CreateInvoiceResponseDto.builder()
                .bolt11(addInvoiceResponse.getPaymentRequest())
                .raw(toJson(addInvoiceResponse))
                .build());
    }

    @GetMapping(value = "/invoice/{hash}")
    public ResponseEntity<JsonNode> lookupInvoice(String paymentHash) throws StatusException, ValidationException {
        PaymentHash request = new PaymentHash();
        request.setRHashStr(paymentHash);

        Invoice invoice = lndApi.lookupInvoice(request);

        return ResponseEntity.ok(toJson(invoice));
    }

    @GetMapping(value = "/network/info")
    public ResponseEntity<JsonNode> getNetworkInfo() throws StatusException, ValidationException {
        NetworkInfo networkInfo = lndApi.getNetworkInfo();
        return ResponseEntity.ok(toJson(networkInfo));
    }

    @GetMapping(value = "/recovery/info")
    public ResponseEntity<JsonNode> getRecoveryInfo() throws StatusException, ValidationException {
        GetRecoveryInfoResponse recoveryInfo = lndApi.getRecoveryInfo();
        return ResponseEntity.ok(toJson(recoveryInfo));
    }

    @GetMapping(value = "/fee/report")
    public ResponseEntity<JsonNode> feeReport() throws StatusException, ValidationException {
        FeeReportResponse feeReport = lndApi.feeReport();
        return ResponseEntity.ok(toJson(feeReport));
    }

    @GetMapping(value = "/channel/balance")
    public ResponseEntity<JsonNode> channelBalance() throws StatusException, ValidationException {
        ChannelBalanceResponse channelBalance = lndApi.channelBalance();
        return ResponseEntity.ok(toJson(channelBalance));
    }

    @GetMapping(value = "/wallet/balance")
    public ResponseEntity<JsonNode> walletBalance() throws StatusException, ValidationException {
        WalletBalanceResponse walletBalance = lndApi.walletBalance();
        return ResponseEntity.ok(toJson(walletBalance));
    }

    private JsonNode toJson(Message<?> message) {
        try {
            return objectMapper.readTree(message.toJsonAsString(false));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

}
