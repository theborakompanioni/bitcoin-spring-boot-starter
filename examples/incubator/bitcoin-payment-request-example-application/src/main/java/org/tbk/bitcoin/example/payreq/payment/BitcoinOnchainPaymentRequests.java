package org.tbk.bitcoin.example.payreq.payment;

import org.bitcoinj.core.Address;
import org.jmolecules.ddd.types.Association;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.tbk.bitcoin.example.payreq.order.Order;

import java.util.List;
import java.util.Optional;

interface BitcoinOnchainPaymentRequests extends CrudRepository<BitcoinOnchainPaymentRequest, PaymentRequest.PaymentRequestId>,
        PagingAndSortingRepository<BitcoinOnchainPaymentRequest, PaymentRequest.PaymentRequestId> {

    /**
     * Returns the payment registered for the given {@link Order}.
     */
    default Optional<BitcoinOnchainPaymentRequest> findByOrder(Order.OrderId id) {
        return findByOrder(Association.forId(id));
    }

    Optional<BitcoinOnchainPaymentRequest> findByOrder(Association<Order, Order.OrderId> order);

    List<BitcoinOnchainPaymentRequest> findByStatus(PaymentRequest.Status status);

    default Optional<LndInvoicePaymentRequest> findByAddress(Address address) {
        return findByAddress(address.toString());
    }

    Optional<LndInvoicePaymentRequest> findByAddress(String address);
}