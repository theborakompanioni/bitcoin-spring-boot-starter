package org.tbk.bitcoin.example.payreq.payment;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
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

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestServiceImpl implements PaymentRequestService {
    private static final CurrencyUnit bitcoin = Monetary.getCurrency("BTC");

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

        AddInvoiceResponse response;
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
    public Optional<PaymentRequest> findPaymentRequestFor(Order order) {
        return paymentRequests.findByOrder(order.getId());
    }

    @Override
    public Optional<PaymentRequest> findPaymentRequestBy(PaymentRequest.PaymentRequestId paymentRequestId) {
        return paymentRequests.findById(paymentRequestId);
    }

    @Override
    public PaymentRequest reevaluatePaymentRequest(PaymentRequest paymentRequest) {
        if (paymentRequest instanceof BitcoinOnchainPaymentRequest) {
            BitcoinOnchainPaymentRequest onchainPaymentRequest = (BitcoinOnchainPaymentRequest) paymentRequest;
            return reevaluateOnchainBitcoinPaymentRequest(onchainPaymentRequest);
        } else {
            throw new UnsupportedOperationException("Cannot yet reevaluate lightning payments");
        }
    }

    private PaymentRequest reevaluateOnchainBitcoinPaymentRequest(BitcoinOnchainPaymentRequest onchainPaymentRequest) {
        boolean stillValid = Instant.now().isBefore(onchainPaymentRequest.getValidUntil());
        if (!stillValid) {
            log.debug("Payment request {} is expired and won't be reevaluated", onchainPaymentRequest.getId().getId());
            return paymentRequests.save(onchainPaymentRequest.markExpired());
        }

        Address address = onchainPaymentRequest.toBitcoinjAddress();
        log.info("Checking balance of address {} for payment request {}", address, onchainPaymentRequest.getId().getId());

        Coin coinsReceivedByAddress;
        try {
            coinsReceivedByAddress = bitcoinClient.getReceivedByAddress(address);
        } catch (IOException e) {
            throw new RuntimeException("Error while executing 'getReceivedByAddress' via bitcoin api", e);
        }

        MonetaryAmount expectedAmount = onchainPaymentRequest.getAmount();
        MonetaryAmount receivedAmount = Money.ofMinor(bitcoin, coinsReceivedByAddress.getValue());

        boolean fullyPaid = receivedAmount.isGreaterThanOrEqualTo(expectedAmount);
        if (fullyPaid) {
            log.info("Marking payment request {} as paid", onchainPaymentRequest.getId().getId());
            return paymentRequests.save(onchainPaymentRequest.markCompleted());
        } else {
            log.info("Payment request {} has not been paid yet", onchainPaymentRequest.getId().getId());
        }

        return onchainPaymentRequest;
    }
}
