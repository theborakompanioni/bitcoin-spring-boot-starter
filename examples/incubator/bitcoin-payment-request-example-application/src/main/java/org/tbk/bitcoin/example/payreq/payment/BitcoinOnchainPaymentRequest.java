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

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;


/**
 * A {@link PaymentRequest} done through a {@link BitcoinOnchainPaymentRequest}.
 */
@Getter
public class BitcoinOnchainPaymentRequest extends PaymentRequest {
    private static final int DEFAULT_MIN_CONFIRMATIONS = 6;

    private final Instant validUntil;

    private final String network;

    private final String address;

    private final int minConfirmations;

    /**
     * Creates a new {@link BitcoinOnchainPaymentRequest} referring to the given {@link Order}.
     * Using {@code DEFAULT_MIN_CONFIRMATIONS} as minimum amount of confirmations.
     *
     * @param order must not be {@literal null}.
     * @param validUntil must not be {@literal null}.
     * @param address must not be {@literal null}.
     */
    BitcoinOnchainPaymentRequest(Order order, Instant validUntil, Address address) {
        this(order, validUntil, address, DEFAULT_MIN_CONFIRMATIONS);
    }

    /**
     * Creates a new {@link BitcoinOnchainPaymentRequest} referring to the given {@link Order}.
     *
     * @param order must not be {@literal null}.
     * @param validUntil must not be {@literal null}.
     * @param address must not be {@literal null}.
     * @param minConfirmations must not be negative.
     */
    BitcoinOnchainPaymentRequest(Order order, Instant validUntil, Address address, int minConfirmations) {
        super(order);
        this.validUntil = requireNonNull(validUntil);
        this.address = address.toString();
        this.network = Network.fromNetworkParameters(address.getParameters()).name();

        checkArgument(minConfirmations >= 0, "'minConfirmations' must not be negative");
        this.minConfirmations = minConfirmations;

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
