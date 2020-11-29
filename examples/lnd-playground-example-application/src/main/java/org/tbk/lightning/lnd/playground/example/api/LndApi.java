package org.tbk.lightning.lnd.playground.example.api;

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

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/lnd", produces = "application/json")
@RequiredArgsConstructor
public class LndApi {

    @NonNull
    private final SynchronousLndAPI lndApi;

    @GetMapping(value = "/info")
    public ResponseEntity<GetInfoResponse> getInfo() throws StatusException, ValidationException {
        GetInfoResponse info = lndApi.getInfo();
        return ResponseEntity.ok(info);
    }

    @GetMapping(value = "/network/info")
    public ResponseEntity<NetworkInfo> getNetworkInfo() throws StatusException, ValidationException {
        NetworkInfo networkInfo = lndApi.getNetworkInfo();
        return ResponseEntity.ok(networkInfo);
    }

    @GetMapping(value = "/recovery/info")
    public ResponseEntity<GetRecoveryInfoResponse> getRecoveryInfo() throws StatusException, ValidationException {
        GetRecoveryInfoResponse recoveryInfo = lndApi.getRecoveryInfo();
        return ResponseEntity.ok(recoveryInfo);
    }

    @GetMapping(value = "/fee/report")
    public ResponseEntity<FeeReportResponse> feeReport() throws StatusException, ValidationException {
        FeeReportResponse feeReport = lndApi.feeReport();
        return ResponseEntity.ok(feeReport);
    }

    @GetMapping(value = "/channel/balance")
    public ResponseEntity<ChannelBalanceResponse> channelBalance() throws StatusException, ValidationException {
        ChannelBalanceResponse channelBalance = lndApi.channelBalance();
        return ResponseEntity.ok(channelBalance);
    }

    @GetMapping(value = "/wallet/balance")
    public ResponseEntity<WalletBalanceResponse> walletBalance() throws StatusException, ValidationException {
        WalletBalanceResponse walletBalance = lndApi.walletBalance();
        return ResponseEntity.ok(walletBalance);
    }

    @GetMapping(value = "/invoice/{hash}")
    public ResponseEntity<Invoice> lookupInvoice(String paymentHash) throws StatusException, ValidationException {
        PaymentHash request = new PaymentHash();
        request.setRHashStr(paymentHash);

        Invoice info = lndApi.lookupInvoice(request);

        return ResponseEntity.ok(info);
    }

    @PostMapping(value = "/invoice")
    public ResponseEntity<AddInvoiceResponse> addInvoice(@RequestBody LightningApi.Invoice invoice) throws StatusException, ValidationException {
        AddInvoiceResponse addInvoiceResponse = lndApi.addInvoice(new Invoice(invoice));
        return ResponseEntity.ok(addInvoiceResponse);
    }
}
