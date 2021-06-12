package org.tbk.bitcoin.example.payreq.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.tbk.bitcoin.example.payreq.common.Network;
import org.tbk.bitcoin.example.payreq.order.Order;

import java.time.Instant;


/**
 * A {@link PaymentRequest} done through a {@link BitcoinOnchainPaymentRequest}.
 */
@Getter
public class BitcoinOnchainPaymentRequest extends PaymentRequest<BitcoinOnchainPaymentRequest> {

    private final long validUntil;

    private final long amount;

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
        this.amount = order.getPrice();
    }

    @JsonProperty
    public String getPaymentUrl() {
        // String.format("bitcoin:%s?amount={}&label={}");
        return String.format("bitcoin:%s?amount=%s", address, Coin.valueOf(this.amount).toPlainString());
    }
}
