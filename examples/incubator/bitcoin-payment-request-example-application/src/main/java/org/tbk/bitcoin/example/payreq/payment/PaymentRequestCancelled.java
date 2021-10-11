package org.tbk.bitcoin.example.payreq.payment;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class PaymentRequestCancelled implements PaymentRequestStateChanged {

    private final PaymentRequest.PaymentRequestId paymentRequestId;

    /**
     * Creates a new {@link PaymentRequestCancelled}.
     *
     * @param paymentRequestId the id of the {@link PaymentRequest} that just has been cancelled
     */
    public PaymentRequestCancelled(PaymentRequest.PaymentRequestId paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
    }
}