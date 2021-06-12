package org.tbk.bitcoin.example.payreq.donation;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbk.bitcoin.example.payreq.common.Network;
import org.tbk.bitcoin.example.payreq.donation.api.query.DonationForm;
import org.tbk.bitcoin.example.payreq.order.LineItem;
import org.tbk.bitcoin.example.payreq.order.Order;
import org.tbk.bitcoin.example.payreq.order.OrderService;
import org.tbk.bitcoin.example.payreq.payment.BitcoinOnchainPaymentRequest;
import org.tbk.bitcoin.example.payreq.payment.PaymentRequestService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

@Service
@RequiredArgsConstructor
class DonationServiceImpl implements DonationService {

    @NonNull
    private final Donations donations;

    @NonNull
    private final PaymentRequestService paymentRequestService;

    @NonNull
    private final OrderService orderService;

    @Override
    @Transactional
    public Donation create(DonationForm form) {
        Network network = form.getNetwork()
                .map(Network::valueOf)
                .orElse(Network.mainnet);

        LineItem lineItem = new LineItem("Donation", 123);

        Order order = orderService.createOrder(Collections.singletonList(lineItem));

        Instant now = Instant.now();
        Instant paymentRequestValidUntil = now.plus(5, ChronoUnit.MINUTES);
        BitcoinOnchainPaymentRequest paymentRequest = (BitcoinOnchainPaymentRequest) paymentRequestService.create(order, network, paymentRequestValidUntil);

        Donation donation = new Donation(order, paymentRequest);
        donation.setComment(form.getComment().orElse(null));

        return donations.save(donation);
    }
}
