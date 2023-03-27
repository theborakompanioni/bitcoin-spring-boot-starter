package org.tbk.bitcoin.example.payreq.payment;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tbk.bitcoin.example.payreq.bitcoin.block.BitcoinBlock;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
class BitcoinOnchainPaymentListener {

    @NonNull
    private final PaymentRequestService paymentRequestService;

    @NonNull
    private final BitcoinOnchainPaymentRequests bitcoinOnchainPaymentRequests;

    @Async
    @TransactionalEventListener
    void onConfirmationEvent(BitcoinBlock.BitcoinBlockCreatedEvent event) {
        List<BitcoinOnchainPaymentRequest> paymentRequests = bitcoinOnchainPaymentRequests
                .findByStatus(PaymentRequest.Status.PAYMENT_EXPECTED);

        log.info("Found {} onchain payment requests waiting to be paid", paymentRequests.size());

        for (BitcoinOnchainPaymentRequest paymentRequest : paymentRequests) {
            try {
                paymentRequestService.reevaluatePaymentRequestById(paymentRequest.getId());
            } catch (Exception e) {
                log.error("Error while reevaluating payment request in loop - continue with next item", e);
            }
        }
    }
}
