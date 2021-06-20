package org.tbk.bitcoin.example.payreq.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.Value;
import org.bitcoinj.core.NetworkParameters;
import org.tbk.bitcoin.example.payreq.common.Network;
import org.tbk.bitcoin.example.payreq.order.Order;

import java.time.Instant;

import static java.util.Objects.requireNonNull;


/**
 * A {@link PaymentRequest} done through a {@link LightningPaymentRequest}.
 */
@Getter
public class LightningPaymentRequest extends PaymentRequest {

    private final Instant validUntil;

    private final String network;

    private final String address;

    /**
     * Creates a new {@link LightningPaymentRequest} referring to the given {@link Order}.
     *
     * @param order must not be {@literal null}.
     */
    protected LightningPaymentRequest(Order order, Instant validUntil, NetworkParameters network, String address) {
        super(order);
        this.validUntil = requireNonNull(validUntil);
        this.network = Network.fromNetworkParameters(network).name();
        this.address = requireNonNull(address);

        registerEvent(BitcoinOnchainPaymentRequestCreatedEvent.of(this.getId()));
    }

    @Override
    @JsonProperty
    public String getPaymentUrl() {
        return String.format("lightning:%s", address);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("validUntil", getValidUntil())
                .add("displayPrice", this.getDisplayPrice())
                .add("network", this.network)
                .add("address", getAddress())
                .toString();
    }

    @Value(staticConstructor = "of")
    public static class BitcoinOnchainPaymentRequestCreatedEvent {

        PaymentRequestId domainId;

        public String toString() {
            return "BitcoinOnchainPaymentRequestCreatedEvent";
        }
    }
}
