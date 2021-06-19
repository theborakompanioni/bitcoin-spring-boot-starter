package org.tbk.bitcoin.example.payreq.payment;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Association;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.util.Assert;
import org.tbk.bitcoin.example.payreq.order.Order;

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
public abstract class PaymentRequest<T extends PaymentRequest<T>>
        extends AbstractAggregateRoot<T>
        implements AggregateRoot<T, PaymentRequest.PaymentRequestId> {

    private final PaymentRequestId id;

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
        this.order = Association.forAggregate(order);
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
