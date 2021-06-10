package org.tbk.bitcoin.example.payreq.invoice;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.AfterDomainEventPublication;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NON_PRIVATE;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "invoice_request")
@EqualsAndHashCode(of = "id", callSuper = false)
@JsonAutoDetect(creatorVisibility = NON_PRIVATE)
public class Invoice extends AbstractAggregateRoot<Invoice> {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    private long createdAt;

    private long validUntil;

    private String network;

    private String comment;

    @AfterDomainEventPublication
    void afterDomainEventPublication() {
        log.info("AfterDomainEventPublication");
    }

    void created() {
        registerEvent(new InvoiceCreatedEvent(this));
    }

    @Value(staticConstructor = "of")
    public static class InvoiceId {
        String id;
    }

    @Value(staticConstructor = "of")
    public static class InvoiceCreatedEvent {

        Invoice invoice;

        public String toString() {
            return "InvoiceCreatedEvent";
        }
    }
}
