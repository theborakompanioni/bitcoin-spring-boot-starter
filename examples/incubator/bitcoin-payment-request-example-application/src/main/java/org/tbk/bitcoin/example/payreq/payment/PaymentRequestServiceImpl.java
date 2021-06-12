package org.tbk.bitcoin.example.payreq.payment;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bitcoinj.core.Address;
import org.jmolecules.ddd.annotation.Service;
import org.tbk.bitcoin.example.payreq.common.Network;
import org.tbk.bitcoin.example.payreq.order.Order;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PaymentRequestServiceImpl implements PaymentRequestService {

    @NonNull
    private final BitcoinClient bitcoinClient;

    @NonNull
    private final PaymentRequests paymentRequests;

    @Override
    public PaymentRequest<?> create(Order order, Network network, Instant validUntil) {
        boolean networkSupported = bitcoinClient.getNetParams().equals(network.toNetworkParameters());
        if (!networkSupported) {
            throw new IllegalArgumentException("Network not supported");
        }

        Address newAddress = null;
        try {
            newAddress = bitcoinClient.getNewAddress();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BitcoinOnchainPaymentRequest paymentRequest = new BitcoinOnchainPaymentRequest(order, validUntil, newAddress);

        return paymentRequests.save(paymentRequest);
    }

    @Override
    public Optional<PaymentRequest<?>> getPaymentRequestFor(Order order) {
        return Optional.empty();
    }
}
