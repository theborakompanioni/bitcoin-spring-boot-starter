package org.tbk.lightning.playground.example.util;

import fr.acinq.lightning.payment.Bolt11Invoice;
import fr.acinq.lightning.payment.Bolt12Invoice;
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

    public static Mono<Bolt11Invoice> decodeBolt11Invoice(String invoice) {
        return decodeInvoice(invoice)
                .filter(it -> it instanceof Bolt11Invoice)
                .cast(Bolt11Invoice.class);
    }

    public static Mono<Bolt12Invoice> decodeBolt12Invoice(String invoice) {
        return decodeInvoice(invoice)
                .filter(it -> it instanceof Bolt12Invoice)
                .cast(Bolt12Invoice.class);
    }

    private InvoiceUtils() {
        throw new UnsupportedOperationException();
    }
}
