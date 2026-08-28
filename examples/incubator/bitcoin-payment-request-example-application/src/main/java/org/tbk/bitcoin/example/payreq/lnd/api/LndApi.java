package org.tbk.bitcoin.example.payreq.lnd.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.lightningj.lnd.proto.LightningApi;
import org.lightningj.lnd.wrapper.Message;
import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.ValidationException;
import org.lightningj.lnd.wrapper.message.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/lnd", produces = "application/json")
@RequiredArgsConstructor
public class LndApi {

    @NonNull
    private final SynchronousLndAPI lndApi;

    @NonNull
    private final ObjectMapper objectMapper;

    @GetMapping(value = "/info")
    public ResponseEntity<String> getInfo() throws StatusException, ValidationException {
        GetInfoResponse info = lndApi.getInfo();
        return ResponseEntity.ok(info.toJsonAsString(true));
    }

    @GetMapping(value = "/network/info")
    public ResponseEntity<String> getNetworkInfo() throws StatusException, ValidationException {
        NetworkInfo networkInfo = lndApi.getNetworkInfo();
        return ResponseEntity.ok(networkInfo.toJsonAsString(true));
    }

    @GetMapping(value = "/recovery/info")
    public ResponseEntity<String> getRecoveryInfo() throws StatusException, ValidationException {
        GetRecoveryInfoResponse recoveryInfo = lndApi.getRecoveryInfo();
        return ResponseEntity.ok(recoveryInfo.toJsonAsString(true));
    }

    @GetMapping(value = "/fee/report")
    public ResponseEntity<String> feeReport() throws StatusException, ValidationException {
        FeeReportResponse feeReport = lndApi.feeReport();
        return ResponseEntity.ok(feeReport.toJsonAsString(true));
    }

    @GetMapping(value = "/channel/balance")
    public ResponseEntity<String> channelBalance() throws StatusException, ValidationException {
        ChannelBalanceResponse channelBalance = lndApi.channelBalance();
        return ResponseEntity.ok(channelBalance.toJsonAsString(true));
    }

    @GetMapping(value = "/wallet/balance")
    public ResponseEntity<String> walletBalance() throws StatusException, ValidationException {
        WalletBalanceResponse walletBalance = lndApi.walletBalance(new WalletBalanceRequest());
        return ResponseEntity.ok(walletBalance.toJsonAsString(true));
    }

    @GetMapping(value = "/invoice/{hash}")
    public ResponseEntity<String> lookupInvoice(String paymentHash) throws StatusException, ValidationException {
        PaymentHash request = new PaymentHash();
        request.setRHashStr(paymentHash);

        Invoice info = lndApi.lookupInvoice(request);

        return ResponseEntity.ok(info.toJsonAsString(true));
    }

    @PostMapping(value = "/invoice")
    public ResponseEntity<CreateInvoiceResponseDto> addInvoice(@Validated @RequestBody CreateInvoiceRequestDto body) throws StatusException, ValidationException {
        LightningApi.Invoice invoice = LightningApi.Invoice.newBuilder()
                .setValueMsat(body.getMsats())
                .setMemo(body.getMemo().orElse(""))
                .build();

        AddInvoiceResponse addInvoiceResponse = lndApi.addInvoice(new Invoice(invoice));

        return ResponseEntity.ok(CreateInvoiceResponseDto.builder()
                .bolt11(addInvoiceResponse.getPaymentRequest())
                .raw(toJson(addInvoiceResponse))
                .build());
    }

    @Value
    @Builder(toBuilder = true)
    @Jacksonized
    public static class CreateInvoiceRequestDto {
        @JsonProperty("memo")
        String memo;

        @Min(1)
        @JsonProperty("msats")
        long msats;

        Optional<String> getMemo() {
            return Optional.ofNullable(memo);
        }
    }

    @Value
    @Builder(toBuilder = true)
    @Jacksonized
    public static class CreateInvoiceResponseDto {
        @NonNull
        @Schema(example = "lnbcrt10p1pj...cpcpugtjt8", requiredMode = Schema.RequiredMode.REQUIRED)
        String bolt11;

        Object raw;
    }

    private JsonNode toJson(Message<?> message) {
        try {
            return objectMapper.readTree(message.toJsonAsString(false));
        } catch (JacksonException e) {
            throw new IllegalStateException(e);
        }
    }

}
