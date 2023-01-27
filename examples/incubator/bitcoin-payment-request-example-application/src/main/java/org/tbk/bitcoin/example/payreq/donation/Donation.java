package org.tbk.bitcoin.example.payreq.donation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Association;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.tbk.bitcoin.example.payreq.order.Order;
import org.tbk.bitcoin.example.payreq.payment.PaymentRequest;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Getter
@Setter(AccessLevel.PACKAGE)
@Table(name = "donation")
@ToString
public class Donation extends AbstractAggregateRoot<Donation> implements AggregateRoot<Donation, Donation.DonationId> {

    private final DonationId id;

    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "order_id")
    private final Association<Order, Order.OrderId> order;

    @Column(name = "payment_request_id")
    private final Association<PaymentRequest, PaymentRequest.PaymentRequestId> paymentRequest;

    private String description;

    private String paymentUrl;

    private String displayPrice;

    private String comment;

    @JsonIgnore
    @Version
    private Long version;

    Donation(Order order, PaymentRequest paymentRequest) {
        this.id = DonationId.create();
        this.order = Association.forAggregate(order);
        this.paymentRequest = Association.forAggregate(paymentRequest);
        this.paymentUrl = paymentRequest.getPaymentUrl();
        this.displayPrice = paymentRequest.getDisplayPrice();

        registerEvent(new DonationCreatedEvent(this.id));
    }

    @AfterDomainEventPublication
    void afterDomainEventPublication() {
        log.trace("AfterDomainEventPublication");
        super.clearDomainEvents();
    }

    @Value(staticConstructor = "of")
    public static class DonationId implements Identifier {
        public static DonationId create() {
            return DonationId.of(UUID.randomUUID().toString());
        }

        @NonNull
        String id;
    }

    @Value(staticConstructor = "of")
    public static class DonationCreatedEvent {

        @NonNull
        DonationId domainId;

        public String toString() {
            return "DonationCreatedEvent";
        }
    }
}
