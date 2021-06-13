package org.tbk.bitcoin.example.payreq.invoice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Association;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.tbk.bitcoin.example.payreq.order.Order;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Version;
import java.time.Instant;
import java.util.UUID;

import static com.google.gdata.util.common.base.Preconditions.checkArgument;

@Slf4j
@Getter
@Setter(AccessLevel.PACKAGE)
@Table(name = "invoice")
public class Invoice extends AbstractAggregateRoot<Invoice> implements AggregateRoot<Invoice, Invoice.InvoiceId> {

    private final InvoiceId id;

    private final long createdAt;

    @Column(name = "order_id")
    private final Association<Order, Order.OrderIdentifier> order;

    private String comment;

    @JsonIgnore
    @Version
    private Long version;

    Invoice(Order order) {
        checkArgument(order != null, "Order must not be null");
        checkArgument(order.isPaid(), "Order not paid");

        this.id = InvoiceId.of(UUID.randomUUID().toString());
        this.createdAt = Instant.now().toEpochMilli();
        this.order = Association.forAggregate(order);

        registerEvent(new InvoiceCreatedEvent(this));
    }

    @AfterDomainEventPublication
    void afterDomainEventPublication() {
        log.trace("AfterDomainEventPublication");
    }

    @Value(staticConstructor = "of")
    public static class InvoiceId implements Identifier {
        public static InvoiceId create() {
            return InvoiceId.of(UUID.randomUUID().toString());
        }

        String id;
    }

    @Value(staticConstructor = "of")
    public static class InvoiceCreatedEvent {

        Invoice domain;

        public String toString() {
            return "InvoiceCreatedEvent";
        }
    }
}
