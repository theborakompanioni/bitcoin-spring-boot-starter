package org.tbk.bitcoin.example.payreq.payment;

import io.grpc.stub.StreamObserver;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lightningj.lnd.wrapper.AsynchronousLndAPI;
import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.ValidationException;
import org.lightningj.lnd.wrapper.message.Invoice;
import org.lightningj.lnd.wrapper.message.InvoiceSubscription;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class LndInvoicePaymentRequestListener {

    @NonNull
    private final AsynchronousLndAPI asynchronousLndAPI;

    @NonNull
    private final LndInvoicePaymentRequests lightningPaymentRequests;

    @NonNull
    private final PaymentRequestService paymentRequestService;

    @Async
    @EventListener(ApplicationReadyEvent.class)
    void onApplicationReady() {
        subscribeToInvoices();
    }

    private void subscribeToInvoices() {
        try {
            InvoiceSubscription invoiceSubscription = new InvoiceSubscription();
            asynchronousLndAPI.subscribeInvoices(invoiceSubscription, new StreamObserver<>() {
                @Override
                public void onNext(Invoice value) {
                    try {
                        String rHash = String.valueOf(Hex.encode(value.getRHash()));
                        lightningPaymentRequests.findByRhash(rHash)
                                .ifPresent(paymentRequestService::reevaluatePaymentRequest);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    log.error("", t);
                }

                @Override
                public void onCompleted() {
                    log.info("Completed subscribing to lnd invoices");
                }
            });
        } catch (StatusException | ValidationException e) {
            log.error("Error while subscribing to lnd invoices", e);
        }
    }
}
