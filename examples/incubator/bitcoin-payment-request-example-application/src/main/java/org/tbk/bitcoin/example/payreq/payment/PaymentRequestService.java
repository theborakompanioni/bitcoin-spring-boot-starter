package org.tbk.bitcoin.example.payreq.payment;

import org.tbk.bitcoin.example.payreq.common.Network;
import org.tbk.bitcoin.example.payreq.order.Order;

import java.time.Instant;
import java.util.Optional;

public interface PaymentRequestService {

    PaymentRequest createOnchainPayment(Order order, Network network, Instant validUntil, int minConfirmations);

    PaymentRequest createLightningPayment(Order order, Network network, Instant validUntil);

    /**
     * Returns the {@link PaymentRequest} for the given {@link Order}.
     *
     * @param order The given order.
     * @return the {@link PaymentRequest} for the given {@link Order} or {@link Optional#empty()} if the Order hasn't been payed yet.
     */
    Optional<PaymentRequest> findPaymentRequestFor(Order order);

    Optional<PaymentRequest> findPaymentRequestBy(PaymentRequest.PaymentRequestId paymentRequestId);

    PaymentRequest reevaluatePaymentRequest(PaymentRequest paymentRequest);
}