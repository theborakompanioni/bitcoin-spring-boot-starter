package org.tbk.bitcoin.example.payreq.payment;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class PaymentRequestCompleted implements PaymentRequestStateChanged {

    private final PaymentRequest.PaymentRequestId paymentRequestId;

    /**
     * Creates a new {@link PaymentRequestCompleted}.
     *
     * @param paymentRequestId the id of the {@link PaymentRequest} that just has been payed
     */
    public PaymentRequestCompleted(PaymentRequest.PaymentRequestId paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
    }
}