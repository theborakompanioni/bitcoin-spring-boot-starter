package org.tbk.bitcoin.example.payreq.bitcoin.block;

import com.google.common.base.MoreObjects;
import jakarta.persistence.Table;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Sha256Hash;
import org.consensusj.bitcoin.json.pojo.BlockInfo;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.AfterDomainEventPublication;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Getter
@Setter(AccessLevel.PACKAGE)
@Table(name = "bitcoin_block")
public class BitcoinBlock extends AbstractAggregateRoot<BitcoinBlock> implements AggregateRoot<BitcoinBlock, BitcoinBlock.BitcoinBlockId> {

    private final BitcoinBlockId id;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    private final Sha256Hash hash;

    private final long time;
    private final long nonce;
    private final long size;
    private final long height;
    private final int version;
    private final Sha256Hash previousblockhash;
    private Sha256Hash nextblockhash;
    private long confirmations;

    BitcoinBlock(Sha256Hash hash,
                 Instant time,
                 long nonce,
                 long confirmations,
                 long size,
                 long height,
                 int version,
                 Sha256Hash previousblockhash,
                 Sha256Hash nextblockhash) {
        this.id = BitcoinBlockId.create();
        this.hash = hash;
        this.time = time.toEpochMilli();
        this.nonce = nonce;
        this.confirmations = confirmations;
        this.size = size;
        this.height = height;
        this.version = version;
        this.previousblockhash = previousblockhash;
        this.nextblockhash = nextblockhash;

        registerEvent(new BitcoinBlockCreatedEvent(this.id));
        registerEvent(new BitcoinBlockConfirmationEvent(this.id, confirmations));
    }

    @AfterDomainEventPublication
    void afterDomainEventPublication() {
        log.trace("AfterDomainEventPublication");
        super.clearDomainEvents();
    }

    void updateMutableValues(BlockInfo blockInfo) {
        this.setNextblockhash(blockInfo.getNextblockhash());

        boolean confirmationUpdate = this.getConfirmations() != blockInfo.getConfirmations();
        if (confirmationUpdate) {
            this.setConfirmations(blockInfo.getConfirmations());
            registerEvent(new BitcoinBlockConfirmationEvent(this.id, blockInfo.getConfirmations()));
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("createdAt", createdAt)
                .add("height", height)
                .add("hash", hash)
                .add("time", time)
                .add("confirmations", confirmations)
                .toString();
    }

    @Value(staticConstructor = "of")
    public static class BitcoinBlockId implements Identifier {
        public static BitcoinBlockId create() {
            return BitcoinBlockId.of(UUID.randomUUID().toString());
        }

        @NonNull
        String id;
    }

    @Value(staticConstructor = "of")
    public static class BitcoinBlockCreatedEvent {

        @NonNull
        BitcoinBlockId domainId;

        public String toString() {
            return "BitcoinBlockCreatedEvent";
        }
    }

    @Value(staticConstructor = "of")
    public static class BitcoinBlockConfirmationEvent {
        @NonNull
        BitcoinBlockId domainId;

        long confirmations;

        public String toString() {
            return "BitcoinBlockConfirmationEvent";
        }
    }
}
