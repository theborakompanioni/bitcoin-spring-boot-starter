package org.tbk.bitcoin.example.payreq.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.Value;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.javamoney.moneta.Money;
import org.tbk.bitcoin.example.payreq.common.Network;
import org.tbk.bitcoin.example.payreq.order.Order;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.NumberValue;
import java.math.BigInteger;
import java.time.Instant;


/**
 * A {@link PaymentRequest} done through a {@link BitcoinOnchainPaymentRequest}.
 */
@Getter
public class BitcoinOnchainPaymentRequest extends PaymentRequest<BitcoinOnchainPaymentRequest> {

    private final long validUntil;

    private final NumberValue amount;

    private final CurrencyUnit currencyUnit;

    private final String displayPrice;

    private final String network;

    private final String address;

    /**
     * Creates a new {@link BitcoinOnchainPaymentRequest} referring to the given {@link Order}.
     *
     * @param order must not be {@literal null}.
     */
    protected BitcoinOnchainPaymentRequest(Order order, Instant validUntil, Address address) {
        super(order);
        this.validUntil = validUntil.toEpochMilli();
        this.address = address.toString();
        this.network = Network.fromNetworkParameters(address.getParameters()).name();

        MonetaryAmount price = order.getPrice();
        this.amount = price.getNumber();
        this.currencyUnit = price.getCurrency();
        this.displayPrice = order.getDisplayPrice();

        registerEvent(BitcoinOnchainPaymentRequestCreatedEvent.of(this.getId()));
    }

    public MonetaryAmount getAmount() {
        return Money.of(this.amount, this.currencyUnit);
    }

    @JsonProperty
    public String getPaymentUrl() {
        // String.format("bitcoin:%s?amount={}&label={}");
        BigInteger satoshi = Money.from(getAmount()).getNumberStripped().unscaledValue();
        return String.format("bitcoin:%s?amount=%s", address, Coin.valueOf(satoshi.longValue()).toPlainString());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("validUntil", getValidUntil())
                .add("displayPrice", this.displayPrice)
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
