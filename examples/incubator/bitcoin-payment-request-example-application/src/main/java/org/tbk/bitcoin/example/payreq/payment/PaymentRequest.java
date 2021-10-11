package org.tbk.bitcoin.example.payreq.payment;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.javamoney.moneta.Money;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Association;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
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

import static com.google.gdata.util.common.base.Preconditions.checkArgument;

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

    private Status status;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "order_id")
    private final Association<Order, Order.OrderId> order;

    /**
     * Creates a new {@link PaymentRequest} referring to the given {@link Order}.
     *
     * @param order must not be {@literal null}.
     */
    protected PaymentRequest(Order order) {
        checkArgument(order != null, "Order must not be null");

        this.id = PaymentRequestId.create();

        MonetaryAmount price = order.getPrice();
        this.amount = price.getNumber();
        this.currencyUnit = price.getCurrency();
        this.displayPrice = order.getDisplayPrice();
        this.status = Status.PAYMENT_EXPECTED;

        this.order = Association.forAggregate(order);
    }

    public MonetaryAmount getAmount() {
        return Money.of(this.amount, this.currencyUnit);
    }

    public abstract String getPaymentUrl();

    /**
     * Returns whether the {@link Order} has been paid already.
     *
     * @return true if order is paid.
     */
    public boolean isOpen() {
        return this.status == Status.PAYMENT_EXPECTED;
    }

    public boolean isCompleted() {
        return this.status == Status.COMPLETED;
    }

    /**
     * Marks the {@link PaymentRequest} as payed.
     */
    public PaymentRequest markCompleted() {
        if (!isOpen()) {
            throw new IllegalStateException("Cannot be completed as request is already final.");
        }

        this.status = Status.COMPLETED;

        registerEvent(new PaymentRequestCompleted(id));

        return this;
    }

    /**
     * Marks the {@link PaymentRequest} as expired.
     */
    public PaymentRequest markExpired() {
        if (!isOpen()) {
            throw new IllegalStateException("Cannot be marked as expired as request is already final.");
        }

        this.status = Status.EXPIRED;

        registerEvent(new PaymentRequestExpired(id));

        return this;
    }

    /**
     * Marks the {@link PaymentRequest} as payed.
     */
    public PaymentRequest markCancelled() {
        if (!isOpen()) {
            throw new IllegalStateException("Cannot be cancelled as request is already final.");
        }

        this.status = Status.CANCELLED;

        registerEvent(new PaymentRequestCancelled(id));

        return this;
    }

    /**
     * Enumeration for all the statuses a {@link PaymentRequest} can be in.
     */
    public enum Status {
        /**
         * {@link PaymentRequest} placed, but not payed yet.
         */
        PAYMENT_EXPECTED,

        /**
         * The {@link PaymentRequest} was completed.
         */
        COMPLETED,

        /**
         * {@link PaymentRequest} was cancelled. No changes allowed to it anymore.
         */
        CANCELLED,

        /**
         * {@link PaymentRequest} was expired. No changes allowed to it anymore.
         */
        EXPIRED;
    }

    @Value(staticConstructor = "of")
    public static class PaymentRequestId implements Identifier {
        public static PaymentRequestId create() {
            return PaymentRequestId.of(UUID.randomUUID().toString());
        }

        @NonNull
        String id;
    }
}
