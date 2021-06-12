package org.tbk.bitcoin.example.payreq.payment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Association;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.util.Assert;
import org.tbk.bitcoin.example.payreq.order.Order;

import javax.persistence.Column;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import java.util.UUID;

/**
 * Baseclass for payment implementations.
 */
@Getter
@ToString
@NoArgsConstructor(force = true)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "payment_request")
public abstract class PaymentRequest<T extends AggregateRoot<T, PaymentRequest.PaymentRequestIdentifier>>
        implements AggregateRoot<T, PaymentRequest.PaymentRequestIdentifier> {

    private final PaymentRequestIdentifier id;

    @Column(name = "order_id")
    private final Association<Order, Order.OrderIdentifier> order;

    /**
     * Creates a new {@link PaymentRequest} referring to the given {@link Order}.
     *
     * @param order must not be {@literal null}.
     */
    protected PaymentRequest(Order order) {
        Assert.notNull(order, "Order must not be null!");

        this.id = PaymentRequestIdentifier.of(UUID.randomUUID().toString());
        this.order = Association.forAggregate(order);
    }

    @Value(staticConstructor = "of")
    public static class PaymentRequestIdentifier implements Identifier {
        String id;
    }
}
