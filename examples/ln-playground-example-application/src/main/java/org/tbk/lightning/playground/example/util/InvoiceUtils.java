package org.tbk.lightning.playground.example.util;

import fr.acinq.lightning.payment.PaymentRequest;
import reactor.core.publisher.Mono;

public final class InvoiceUtils {

    public static Mono<PaymentRequest> decodeInvoice(String invoice) {
        return Mono.fromCallable(() -> {
            try {
                return PaymentRequest.Companion.read(invoice).get();
            } catch (Exception e) {
                // exception can be of type IllegalArgumentException, ArrayIndexOutOfBoundsException, etc..
                throw new IllegalStateException("Failed to decode invoice.", e);
            }
        });
    }

    private InvoiceUtils() {
        throw new UnsupportedOperationException();
    }
}
