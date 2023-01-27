package org.tbk.bitcoin.example.payreq.lnd.info;

import com.google.common.base.MoreObjects;
import jakarta.persistence.Table;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.AfterDomainEventPublication;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Getter
@Setter(AccessLevel.PACKAGE)
@Table(name = "lnd_info")
public class LndInfo extends AbstractAggregateRoot<LndInfo> implements AggregateRoot<LndInfo, LndInfo.LndInfoId> {

    private final LndInfoId id;

    private final long createdAt;

    private final long blockHeight;

    private final String blockHash;

    private final long bestHeaderTimestamp;

    LndInfo(long blockHeight, String blockHash, long bestHeaderTimestamp) {
        this.id = LndInfoId.of(UUID.randomUUID().toString());
        this.createdAt = Instant.now().toEpochMilli();
        this.blockHeight = blockHeight;
        this.blockHash = blockHash;
        this.bestHeaderTimestamp = bestHeaderTimestamp;

        registerEvent(new LndInfoCreatedEvent(this.id));
    }

    @AfterDomainEventPublication
    void afterDomainEventPublication() {
        log.trace("AfterDomainEventPublication");
        super.clearDomainEvents();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("createdAt", createdAt)
                .add("blockHeight", blockHeight)
                .add("blockHash", blockHash)
                .toString();
    }

    @Value(staticConstructor = "of")
    public static class LndInfoId implements Identifier {
        public static LndInfoId create() {
            return LndInfoId.of(UUID.randomUUID().toString());
        }

        @NonNull
        String id;
    }

    @Value(staticConstructor = "of")
    public static class LndInfoCreatedEvent {

        @NonNull
        LndInfoId domainId;

        public String toString() {
            return "LndInfoCreatedEvent";
        }
    }
}
