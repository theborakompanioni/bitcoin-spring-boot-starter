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
import org.lightningj.lnd.wrapper.message.PaymentHash;
import org.springframework.security.crypto.codec.Hex;
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
    public PaymentRequest createOnchainPayment(Order order, Network network, Instant validUntil, int minConfirmations) {
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

        BitcoinOnchainPaymentRequest paymentRequest = new BitcoinOnchainPaymentRequest(order, validUntil, newAddress, minConfirmations);

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

        LndInvoicePaymentRequest paymentRequest = new LndInvoicePaymentRequest(order, validUntil,
                network.toNetworkParameters(), response.getPaymentRequest(), response.getRHash());

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
        }

        if (paymentRequest instanceof LndInvoicePaymentRequest) {
            LndInvoicePaymentRequest lightningPaymentRequest = (LndInvoicePaymentRequest) paymentRequest;
            return reevaluateLightningPaymentRequest(lightningPaymentRequest);
        }

        throw new UnsupportedOperationException("Cannot yet reevaluate payments of type " + paymentRequest.getClass().getSimpleName());
    }

    private PaymentRequest reevaluateOnchainBitcoinPaymentRequest(BitcoinOnchainPaymentRequest onchainPaymentRequest) {
        boolean stillValid = Instant.now().isBefore(onchainPaymentRequest.getValidUntil());

        if (!stillValid) {
            log.debug("Onchain payment request {} is expired and won't be reevaluated", onchainPaymentRequest.getId().getId());
            onchainPaymentRequest.markExpired();
        } else {
            Address address = onchainPaymentRequest.toBitcoinjAddress();
            log.debug("Checking balance of address {} for onchain payment request {}", address, onchainPaymentRequest.getId().getId());

            Coin coinsReceivedByAddress;
            try {
                coinsReceivedByAddress = bitcoinClient.getReceivedByAddress(address, onchainPaymentRequest.getMinConfirmations());
            } catch (IOException e) {
                throw new RuntimeException("Error while executing 'getReceivedByAddress' via bitcoin api", e);
            }

            MonetaryAmount expectedAmount = onchainPaymentRequest.getAmount();
            MonetaryAmount receivedAmount = Money.ofMinor(bitcoin, coinsReceivedByAddress.getValue());

            boolean fullyPaid = receivedAmount.isGreaterThanOrEqualTo(expectedAmount);
            if (!fullyPaid) {
                log.debug("Onchain payment request {} has not been paid yet", onchainPaymentRequest.getId().getId());
            } else {
                log.info("Marking onchain payment request {} as paid", onchainPaymentRequest.getId().getId());
                onchainPaymentRequest.markCompleted();
            }
        }

        return paymentRequests.save(onchainPaymentRequest);
    }

    private PaymentRequest reevaluateLightningPaymentRequest(LndInvoicePaymentRequest lightningPaymentRequest) {
        boolean stillValid = Instant.now().isBefore(lightningPaymentRequest.getValidUntil());

        if (!stillValid) {
            log.debug("Lightning payment request {} is expired and won't be reevaluated", lightningPaymentRequest.getId().getId());
            lightningPaymentRequest.markExpired();
        } else {

            PaymentHash paymentHash = new PaymentHash();
            paymentHash.setRHash(Hex.decode(lightningPaymentRequest.getRhash()));

            log.debug("Checking balance of invoice {} for lightning payment request {}", lightningPaymentRequest.getRhash(), lightningPaymentRequest.getId().getId());
            Invoice invoice;
            try {
                invoice = lndApi.lookupInvoice(paymentHash);
            } catch (StatusException | ValidationException e) {
                throw new RuntimeException("Error while executing 'lookupInvoice' via lnd api", e);
            }

            if (invoice.getSettled()) {
                MonetaryAmount expectedAmount = lightningPaymentRequest.getAmount();
                MonetaryAmount receivedAmount = Money.ofMinor(bitcoin, invoice.getAmtPaidSat());

                boolean fullyPaid = receivedAmount.isGreaterThanOrEqualTo(expectedAmount);
                if (!fullyPaid) {
                    log.debug("Lightning payment request {} has not been paid yet", lightningPaymentRequest.getId().getId());
                } else {
                    log.info("Marking lightning payment request {} as paid", lightningPaymentRequest.getId().getId());
                    lightningPaymentRequest.markCompleted();
                }
            } else if (invoice.getExpiry() <= 0L) {
                log.debug("Invoice {} is expired and lightning payment request won't be reevaluated", lightningPaymentRequest.getId().getId());
                lightningPaymentRequest.markExpired();
            }
        }

        return paymentRequests.save(lightningPaymentRequest);
    }
}
