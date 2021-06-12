package org.tbk.bitcoin.example.payreq.payment;

import org.tbk.bitcoin.example.payreq.common.Network;
import org.tbk.bitcoin.example.payreq.order.Order;

import java.time.Instant;
import java.util.Optional;

public interface PaymentRequestService {

    PaymentRequest<?> create(Order order, Network network, Instant validUntil);

    /**
     * Returns the {@link PaymentRequest} for the given {@link Order}.
     *
     * @param order
     * @return the {@link PaymentRequest} for the given {@link Order} or {@link Optional#empty()} if the Order hasn't been payed
     * yet.
     */
    Optional<PaymentRequest<?>> getPaymentRequestFor(Order order);
}