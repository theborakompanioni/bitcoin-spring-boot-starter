package org.tbk.bitcoin.example.payreq.invoice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.AfterDomainEventPublication;

import javax.persistence.Table;
import javax.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Getter
@Setter(AccessLevel.PACKAGE)
@Table(name = "invoice")
public class Invoice extends AbstractAggregateRoot<Invoice> implements AggregateRoot<Invoice, Invoice.InvoiceId> {

    private final InvoiceId id;

    private final long createdAt;

    private final long validUntil;

    private String network;

    private String comment;

    @JsonIgnore
    @Version
    private Long version;

    Invoice(Instant validUntil) {
        this.id = InvoiceId.of(UUID.randomUUID().toString());
        this.createdAt = Instant.now().toEpochMilli();
        this.validUntil = validUntil.toEpochMilli();

        registerEvent(new InvoiceCreatedEvent(this));
    }

    Invoice() {
        this(Instant.EPOCH);
    }

    @AfterDomainEventPublication
    void afterDomainEventPublication() {
        log.info("AfterDomainEventPublication");
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
