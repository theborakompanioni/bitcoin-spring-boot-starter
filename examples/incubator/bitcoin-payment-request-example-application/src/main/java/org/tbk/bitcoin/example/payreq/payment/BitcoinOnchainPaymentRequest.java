package org.tbk.bitcoin.example.payreq.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.Value;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.javamoney.moneta.Money;
import org.tbk.bitcoin.example.payreq.common.Network;
import org.tbk.bitcoin.example.payreq.order.Order;

import java.math.BigInteger;
import java.time.Instant;

import static java.util.Objects.requireNonNull;


/**
 * A {@link PaymentRequest} done through a {@link BitcoinOnchainPaymentRequest}.
 */
@Getter
public class BitcoinOnchainPaymentRequest extends PaymentRequest {

    private final Instant validUntil;

    private final String network;

    private final String address;

    /**
     * Creates a new {@link BitcoinOnchainPaymentRequest} referring to the given {@link Order}.
     *
     * @param order must not be {@literal null}.
     */
    protected BitcoinOnchainPaymentRequest(Order order, Instant validUntil, Address address) {
        super(order);
        this.validUntil = requireNonNull(validUntil);
        this.address = address.toString();
        this.network = Network.fromNetworkParameters(address.getParameters()).name();

        registerEvent(BitcoinOnchainPaymentRequestCreatedEvent.of(this.getId()));
    }

    @Override
    @JsonProperty
    public String getPaymentUrl() {
        // String.format("bitcoin:%s?amount={}&label={}");
        BigInteger satoshi = Money.from(getAmount()).getNumberStripped().unscaledValue();
        return String.format("bitcoin:%s?amount=%s", address, Coin.valueOf(satoshi.longValue()).toPlainString());
    }

    public Address toBitcoinjAddress() {
        NetworkParameters network = Network.ofNullable(this.getNetwork())
                .orElseGet(MainNetParams::get);

        return Address.fromString(network, this.getAddress());
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
