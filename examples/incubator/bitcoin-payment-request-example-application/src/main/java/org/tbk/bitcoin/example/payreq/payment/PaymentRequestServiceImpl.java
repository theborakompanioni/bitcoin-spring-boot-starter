package org.tbk.bitcoin.example.payreq.payment;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bitcoinj.core.Address;
import org.javamoney.moneta.Money;
import org.jmolecules.ddd.annotation.Service;
import org.lightningj.lnd.proto.LightningApi;
import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.ValidationException;
import org.lightningj.lnd.wrapper.message.AddInvoiceResponse;
import org.lightningj.lnd.wrapper.message.Invoice;
import org.tbk.bitcoin.example.payreq.common.Network;
import org.tbk.bitcoin.example.payreq.order.Order;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PaymentRequestServiceImpl implements PaymentRequestService {

    @NonNull
    private final BitcoinClient bitcoinClient;

    @NonNull
    private final SynchronousLndAPI lndApi;

    @NonNull
    private final PaymentRequests paymentRequests;

    @Override
    public PaymentRequest createOnchainPayment(Order order, Network network, Instant validUntil) {
        boolean networkSupported = bitcoinClient.getNetParams().equals(network.toNetworkParameters());
        if (!networkSupported) {
            throw new IllegalArgumentException("Network not supported");
        }

        Address newAddress = null;
        try {
            newAddress = bitcoinClient.getNewAddress();
        } catch (IOException e) {
            throw new RuntimeException("Error while executing 'getnewaddress' via bitcoin api", e);
        }

        BitcoinOnchainPaymentRequest paymentRequest = new BitcoinOnchainPaymentRequest(order, validUntil, newAddress);

        return paymentRequests.save(paymentRequest);
    }

    @Override
    public PaymentRequest createLightningPayment(Order order, Network network, Instant validUntil) {
        try {
            boolean networkSupported = lndApi.getInfo().getChains().stream()
                    .filter(it -> "bitcoin".equals(it.getChain()))
                    .anyMatch(it -> network.name().equals(it.getNetwork()));

            if (!networkSupported) {
                throw new IllegalArgumentException("Network not supported");
            }
        } catch (StatusException | ValidationException e) {
            throw new RuntimeException("Error while creating invoice via lnd api", e);
        }

        Instant now = Instant.now();

        AddInvoiceResponse response = null;
        try {
            BigInteger satoshi = Money.from(order.getPrice()).getNumberStripped().unscaledValue();
            LightningApi.Invoice invoice = LightningApi.Invoice.newBuilder()
                    .setCreationDate(now.getEpochSecond())
                    .setExpiry(Duration.between(now, validUntil).toSeconds())
                    .setValue(satoshi.longValueExact())
                    .setMemo("")
                    .build();
            Invoice request = new Invoice(invoice);
            response = lndApi.addInvoice(request);
        } catch (StatusException | ValidationException e) {
            throw new RuntimeException("Error while creating invoice via lnd api", e);
        }

        LightningPaymentRequest paymentRequest = new LightningPaymentRequest(order, validUntil, network.toNetworkParameters(), response.getPaymentRequest());

        return paymentRequests.save(paymentRequest);
    }

    @Override
    public Optional<PaymentRequest> getPaymentRequestFor(Order order) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
