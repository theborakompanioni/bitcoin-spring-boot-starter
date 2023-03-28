package org.tbk.lightning.playground.example.api;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lightningj.lnd.proto.LightningApi;
import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.ValidationException;
import org.lightningj.lnd.wrapper.message.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.testcontainers.shaded.com.google.common.primitives.Longs;

import javax.json.JsonObject;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/lnd", produces = "application/json")
@RequiredArgsConstructor
public class LndApi {

    @NonNull
    private final SynchronousLndAPI lndApi;

    @GetMapping(value = "/info")
    public ResponseEntity<JsonObject> getInfo() throws StatusException, ValidationException {
        GetInfoResponse info = lndApi.getInfo();
        return ResponseEntity.ok(info.toJson().build());
    }

    @GetMapping(value = "/network/info")
    public ResponseEntity<JsonObject> getNetworkInfo() throws StatusException, ValidationException {
        NetworkInfo networkInfo = lndApi.getNetworkInfo();
        return ResponseEntity.ok(networkInfo.toJson().build());
    }

    @GetMapping(value = "/recovery/info")
    public ResponseEntity<JsonObject> getRecoveryInfo() throws StatusException, ValidationException {
        GetRecoveryInfoResponse recoveryInfo = lndApi.getRecoveryInfo();
        return ResponseEntity.ok(recoveryInfo.toJson().build());
    }

    @GetMapping(value = "/fee/report")
    public ResponseEntity<JsonObject> feeReport() throws StatusException, ValidationException {
        FeeReportResponse feeReport = lndApi.feeReport();
        return ResponseEntity.ok(feeReport.toJson().build());
    }

    @GetMapping(value = "/channel/balance")
    public ResponseEntity<JsonObject> channelBalance() throws StatusException, ValidationException {
        ChannelBalanceResponse channelBalance = lndApi.channelBalance();
        return ResponseEntity.ok(channelBalance.toJson().build());
    }

    @GetMapping(value = "/wallet/balance")
    public ResponseEntity<JsonObject> walletBalance() throws StatusException, ValidationException {
        WalletBalanceResponse walletBalance = lndApi.walletBalance();
        return ResponseEntity.ok(walletBalance.toJson().build());
    }

    @GetMapping(value = "/invoice/{hash}")
    public ResponseEntity<JsonObject> lookupInvoice(String paymentHash) throws StatusException, ValidationException {
        PaymentHash request = new PaymentHash();
        request.setRHashStr(paymentHash);

        Invoice info = lndApi.lookupInvoice(request);

        return ResponseEntity.ok(info.toJson().build());
    }

    @PostMapping(value = "/invoice")
    public ResponseEntity<JsonObject> addInvoice(@RequestBody Map<String, Object> body) throws StatusException, ValidationException {

        String memo = Optional.ofNullable(body.get("memo"))
                .map(Object::toString)
                .orElse("");

        long value = Optional.ofNullable(body.get("value"))
                .map(Object::toString)
                .map(it -> Longs.tryParse(it, 10))
                .filter(it -> it > 0)
                .orElseThrow(() -> new IllegalArgumentException("'value' must be a positive integer"));

        LightningApi.Invoice invoice = LightningApi.Invoice.newBuilder()
                .setValue(value)
                .setMemo(memo)
                .build();

        AddInvoiceResponse addInvoiceResponse = lndApi.addInvoice(new Invoice(invoice));
        return ResponseEntity.ok(addInvoiceResponse.toJson().build());
    }
}
