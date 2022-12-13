package org.tbk.bitcoin.example.payreq.payment;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

interface LndInvoicePaymentRequests extends CrudRepository<LndInvoicePaymentRequest, PaymentRequest.PaymentRequestId>,
        PagingAndSortingRepository<LndInvoicePaymentRequest, PaymentRequest.PaymentRequestId> {

    List<LndInvoicePaymentRequest> findByStatus(PaymentRequest.Status status);

    Optional<LndInvoicePaymentRequest> findByRhash(String rHash);
}