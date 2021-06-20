package org.tbk.bitcoin.example.payreq.payment;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.javamoney.moneta.Money;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Association;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.util.Assert;
import org.tbk.bitcoin.example.payreq.order.Order;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.NumberValue;
import javax.persistence.Column;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Baseclass for payment implementations.
 */
@Getter
@ToString
@NoArgsConstructor(force = true)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "payment_request")
public abstract class PaymentRequest
        extends AbstractAggregateRoot<PaymentRequest>
        implements AggregateRoot<PaymentRequest, PaymentRequest.PaymentRequestId> {

    private final PaymentRequestId id;

    private final NumberValue amount;

    private final CurrencyUnit currencyUnit;

    private final String displayPrice;

    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "order_id")
    private final Association<Order, Order.OrderId> order;

    /**
     * Creates a new {@link PaymentRequest} referring to the given {@link Order}.
     *
     * @param order must not be {@literal null}.
     */
    protected PaymentRequest(Order order) {
        Assert.notNull(order, "Order must not be null");

        this.id = PaymentRequestId.create();

        MonetaryAmount price = order.getPrice();
        this.amount = price.getNumber();
        this.currencyUnit = price.getCurrency();
        this.displayPrice = order.getDisplayPrice();

        this.order = Association.forAggregate(order);
    }

    public MonetaryAmount getAmount() {
        return Money.of(this.amount, this.currencyUnit);
    }

    public abstract String getPaymentUrl();

    @Value(staticConstructor = "of")
    public static class PaymentRequestId implements Identifier {
        public static PaymentRequestId create() {
            return PaymentRequestId.of(UUID.randomUUID().toString());
        }

        @NonNull
        String id;
    }
}
