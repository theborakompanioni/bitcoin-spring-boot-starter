package org.tbk.bitcoin.example.payreq.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import org.javamoney.moneta.Money;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.tbk.bitcoin.example.payreq.common.Currencies;
import org.tbk.bitcoin.example.payreq.common.MonetaryAmountFormats;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * An order.
 */
@Getter
@ToString(exclude = "lineItems")
@Table(name = "customer_order")
public class Order extends AbstractAggregateRoot<Order> implements AggregateRoot<Order, Order.OrderIdentifier> {

    private final OrderIdentifier id;
    private final long createdAt;
    private Status status;

    @JsonIgnore
    @Version
    private Long version;

    @JoinColumn(name = "order_id")
    @OrderColumn(name = "position")
    private final List<LineItem> lineItems = new ArrayList<>();

    /**
     * Creates a new {@link Order} for the given {@link LineItem}s.
     *
     * @param lineItems must not be {@literal null}.
     */
    public Order(Collection<LineItem> lineItems) {
        this.id = OrderIdentifier.of(UUID.randomUUID().toString());
        this.createdAt = Instant.now().toEpochMilli();
        this.status = Status.PAYMENT_EXPECTED;
        this.lineItems.addAll(lineItems);
    }

    /**
     * Creates a new {@link Order} containing the given {@link LineItem}s.
     *
     * @param items must not be {@literal null}.
     */
    public Order(LineItem... items) {
        this(List.of(items));
    }

    Order() {
        this(new LineItem[0]);
    }

    /**
     * Returns the price of the {@link Order} calculated from the contained items.
     *
     * @return will never be {@literal null}.
     */
    public MonetaryAmount getPrice() {
        return lineItems.stream().
                map(LineItem::getPrice).
                reduce(MonetaryAmount::add)
                .orElse(Money.of(BigDecimal.ZERO, Currencies.BTC));
    }

    public String getDisplayPrice() {
        MonetaryAmount price = this.getPrice();
        MonetaryAmount rounded = price.with(Monetary.getRounding(price.getCurrency()));
        return MonetaryAmountFormats.bitcoin.format(rounded);
    }

    /**
     * Marks the {@link Order} as payed.
     */
    public Order markPaid() {

        if (isPaid()) {
            throw new IllegalStateException("Already paid order cannot be paid again!");
        }

        this.status = Status.PAID;

        registerEvent(new OrderPaid(id));

        return this;
    }

    /**
     * Marks the {@link Order} as in preparation.
     */
    public Order markInPreparation() {

        if (this.status != Status.PAID) {
            String errorMessage = String.format("Order must be in state payed to start preparation! Current status: %s", this.status);
            throw new IllegalStateException(
                    errorMessage);
        }

        this.status = Status.PREPARING;

        return this;
    }

    /**
     * Marks the {@link Order} as prepared.
     */
    public Order markPrepared() {

        if (this.status != Status.PREPARING) {
            String errorMessage = String.format("Cannot mark Order prepared that is currently not preparing! Current status: %s.", this.status);
            throw new IllegalStateException(errorMessage);
        }

        this.status = Status.READY;

        return this;
    }

    public Order markTaken() {

        if (this.status != Status.READY) {
            String errorMessage = String.format("Cannot mark Order taken that is currently not paid! Current status: %s.", this.status);
            throw new IllegalStateException(errorMessage);
        }

        this.status = Status.TAKEN;

        return this;
    }

    /**
     * Returns whether the {@link Order} has been paid already.
     *
     * @return
     */
    public boolean isPaid() {
        return !this.status.equals(Status.PAYMENT_EXPECTED);
    }

    /**
     * Returns if the {@link Order} is ready to be taken.
     *
     * @return
     */
    public boolean isReady() {
        return this.status.equals(Status.READY);
    }

    public boolean isTaken() {
        return this.status.equals(Status.TAKEN);
    }

    /**
     * Enumeration for all the statuses an {@link Order} can be in.
     */
    public enum Status {

        /**
         * Placed, but not payed yet. Still changeable.
         */
        PAYMENT_EXPECTED,

        /**
         * {@link Order} was payed. No changes allowed to it anymore.
         */
        PAID,

        /**
         * The {@link Order} is currently processed.
         */
        PREPARING,

        /**
         * The {@link Order} is ready to be picked up by the customer.
         */
        READY,

        /**
         * The {@link Order} was completed.
         */
        TAKEN;
    }

    @Value(staticConstructor = "of")
    public static class OrderIdentifier implements Identifier {
        String id;
    }
}