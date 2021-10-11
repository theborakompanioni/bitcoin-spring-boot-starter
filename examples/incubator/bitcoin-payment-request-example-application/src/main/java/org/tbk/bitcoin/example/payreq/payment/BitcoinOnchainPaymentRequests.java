package org.tbk.bitcoin.example.payreq.payment;

import org.jmolecules.ddd.types.Association;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.tbk.bitcoin.example.payreq.order.Order;

import java.util.List;
import java.util.Optional;

interface BitcoinOnchainPaymentRequests extends PagingAndSortingRepository<BitcoinOnchainPaymentRequest, PaymentRequest.PaymentRequestId> {

    /**
     * Returns the payment registered for the given {@link Order}.
     */
    default Optional<BitcoinOnchainPaymentRequest> findByOrder(Order.OrderId id) {
        return findByOrder(Association.forId(id));
    }

    Optional<BitcoinOnchainPaymentRequest> findByOrder(Association<Order, Order.OrderId> order);

    List<BitcoinOnchainPaymentRequest> findByStatus(@Param("status") PaymentRequest.Status status);
}