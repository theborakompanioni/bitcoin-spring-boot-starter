package org.tbk.bitcoin.example.payreq.payment;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class PaymentRequestExpired implements PaymentRequestStateChanged {

    private final PaymentRequest.PaymentRequestId paymentRequestId;

    /**
     * Creates a new {@link PaymentRequestExpired}.
     *
     * @param paymentRequestId the id of the {@link PaymentRequest} that just has been expired
     */
    public PaymentRequestExpired(PaymentRequest.PaymentRequestId paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
    }
}