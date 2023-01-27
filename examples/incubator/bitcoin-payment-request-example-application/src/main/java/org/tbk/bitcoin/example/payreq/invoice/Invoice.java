package org.tbk.bitcoin.example.payreq.invoice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Association;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.tbk.bitcoin.example.payreq.order.Order;

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
    private final Association<Order, Order.OrderId> order;

    private String comment;

    @JsonIgnore
    @Version
    private Long version;

    Invoice(Order order) {
        checkArgument(order != null, "Order must not be null");
        checkArgument(order.isCompleted(), "Order not completed");

        this.id = InvoiceId.create();
        this.createdAt = Instant.now().toEpochMilli();
        this.order = Association.forAggregate(order);

        registerEvent(new InvoiceCreatedEvent(this.id));
    }

    @AfterDomainEventPublication
    void afterDomainEventPublication() {
        log.trace("AfterDomainEventPublication");
        super.clearDomainEvents();
    }

    @Value(staticConstructor = "of")
    public static class InvoiceId implements Identifier {
        public static InvoiceId create() {
            return InvoiceId.of(UUID.randomUUID().toString());
        }

        @NonNull
        String id;
    }

    @Value(staticConstructor = "of")
    public static class InvoiceCreatedEvent {

        @NonNull
        InvoiceId domainId;

        public String toString() {
            return "InvoiceCreatedEvent";
        }
    }
}
