package org.tbk.bitcoin.example.payreq.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.Value;
import org.bitcoinj.core.NetworkParameters;
import org.springframework.security.crypto.codec.Hex;
import org.tbk.bitcoin.example.payreq.common.Network;
import org.tbk.bitcoin.example.payreq.order.Order;

import javax.persistence.Column;
import java.time.Instant;

import static java.util.Objects.requireNonNull;


/**
 * A {@link PaymentRequest} done through a {@link LndInvoicePaymentRequest}.
 */
@Getter
public class LndInvoicePaymentRequest extends PaymentRequest {

    private final Instant validUntil;

    private final String network;

    private final String paymentHash;

    @Column(name = "r_hash")
    private final String rhash;

    /**
     * Creates a new {@link LndInvoicePaymentRequest} referring to the given {@link Order}.
     *
     * @param order       must not be {@literal null}.
     * @param network     must not be {@literal null}.
     * @param paymentHash must not be {@literal null}.
     */
    LndInvoicePaymentRequest(Order order, Instant validUntil, NetworkParameters network,
                             String paymentHash, byte[] rHash) {
        super(order);
        this.validUntil = requireNonNull(validUntil);
        this.network = Network.fromNetworkParameters(network).name();
        this.paymentHash = requireNonNull(paymentHash);
        this.rhash = String.valueOf(Hex.encode(rHash));

        registerEvent(LightningPaymentRequestCreatedEvent.of(this.getId()));
    }

    @Override
    @JsonProperty
    public String getPaymentUrl() {
        return String.format("lightning:%s", paymentHash);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("validUntil", getValidUntil())
                .add("displayPrice", getDisplayPrice())
                .add("network", network)
                .add("paymentHash", paymentHash)
                .add("rhash", rhash)
                .toString();
    }

    @Value(staticConstructor = "of")
    public static class LightningPaymentRequestCreatedEvent {

        PaymentRequestId domainId;

        public String toString() {
            return "LightningPaymentRequestCreatedEvent";
        }
    }
}
