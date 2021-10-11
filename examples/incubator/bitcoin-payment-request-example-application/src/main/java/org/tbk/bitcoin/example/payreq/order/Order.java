package org.tbk.bitcoin.example.payreq.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.javamoney.moneta.Money;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.tbk.bitcoin.example.payreq.common.Currencies;
import org.tbk.bitcoin.example.payreq.common.MonetaryAmountFormats;
import org.tbk.bitcoin.example.payreq.exchangerate.ExchangeRate;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * An order.
 */
@Getter
@ToString(exclude = "lineItems")
@Table(name = "customer_order")
public class Order extends AbstractAggregateRoot<Order> implements AggregateRoot<Order, Order.OrderId> {

    private final OrderId id;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

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
    Order(Collection<LineItem> lineItems) {
        this.id = OrderId.create();
        this.status = Status.CREATED;
        this.lineItems.addAll(lineItems);

        registerEvent(OrderCreated.of(this.id));
    }

    /**
     * Returns the price of the {@link Order} calculated from the contained items.
     *
     * @return will never be {@literal null}.
     */
    public MonetaryAmount getPrice() {
        return lineItems.stream()
                .map(LineItem::getPrice)
                .reduce(MonetaryAmount::add)
                .orElse(Money.of(BigDecimal.ZERO, Currencies.BTC));
    }

    public String getDisplayPrice() {
        MonetaryAmount price = this.getPrice();
        MonetaryAmount rounded = price.with(Monetary.getRounding(price.getCurrency()));
        return MonetaryAmountFormats.bitcoin.format(rounded);
    }

    /**
     * Marks the {@link Order} as ready.
     */
    public Order markReady() {
        this.status = this.status.transitTo(Status.READY);
        registerEvent(OrderReady.of(id));
        return this;
    }

    /**
     * Marks the {@link Order} as in preparation.
     */
    public Order markInProgress() {
        this.status = this.status.transitTo(Status.IN_PROGRESS);
        registerEvent(OrderInProgress.of(id));
        return this;
    }

    /**
     * Marks the {@link Order} as prepared.
     */
    public Order markCompleted() {
        this.status = this.status.transitTo(Status.COMPLETED);
        registerEvent(OrderCompleted.of(id));
        return this;
    }

    /**
     * Marks the {@link Order} as prepared.
     */
    public Order markError() {
        this.status = this.status.transitTo(Status.ERROR);
        registerEvent(OrderError.of(id));
        return this;
    }

    public Order markCancelled() {
        this.status = this.status.transitTo(Status.CANCELLED);
        registerEvent(OrderCancelled.of(id));
        return this;
    }

    /**
     * Returns if the {@link Order} is in process or ready to be processed.
     *
     * @return true if order can be reevaluated.
     */
    public boolean canReevaluate() {
        return this.status == Status.READY
                || this.status == Status.IN_PROGRESS;
    }

    /**
     * Returns if the {@link Order} is currently processed.
     *
     * @return true if order in progress.
     */
    public boolean isInProgress() {
        return this.status == Status.IN_PROGRESS;
    }

    /**
     * Returns if the {@link Order} is completed.
     *
     * @return true if order completed.
     */
    public boolean isCompleted() {
        return this.status == Status.COMPLETED;
    }

    /**
     * Enumeration for all the statuses an {@link Order} can be in.
     */
    public enum Status {
        /**
         * Placed, but not payed yet. Still changeable.
         */
        CREATED,

        /**
         * Placed, paid and ready. No changes allowed to it anymore.
         */
        READY,

        /**
         * The {@link Order} is currently processed. Order can be partially executed at this stage.
         */
        IN_PROGRESS,

        /**
         * The {@link Order} was completed.
         */
        COMPLETED,

        /**
         * The {@link Order} is cancelled.
         */
        CANCELLED,

        /**
         * The {@link Order} processing resulted in an error.
         */
        ERROR;

        static Map<Status, Set<Status>> transitions = ImmutableMap.<Status, Set<Status>>builder()
                .put(CREATED, Set.of(READY, CANCELLED))
                .put(READY, Set.of(CANCELLED, IN_PROGRESS))
                .put(IN_PROGRESS, Set.of(COMPLETED, ERROR))
                .put(COMPLETED, Collections.emptySet())
                .put(CANCELLED, Collections.emptySet())
                .put(ERROR, Collections.emptySet())
                .build();

        boolean isTransitionAllowed(Status status) {
            return transitions.get(this).contains(status);
        }

        Status transitTo(Status status) {
            if (!this.isTransitionAllowed(status)) {
                String errorMessage = String.format("Transition from %s to %s is not allowed.", this, status);
                throw new IllegalStateException(errorMessage);
            }
            return status;
        }
    }

    @Value(staticConstructor = "of")
    public static class OrderId implements Identifier {
        public static OrderId create() {
            return OrderId.of(UUID.randomUUID().toString());
        }

        @NonNull
        String id;
    }

}