package org.tbk.bitcoin.example.payreq.bitcoin.chaininfo;

import com.google.common.base.MoreObjects;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Sha256Hash;
import org.hibernate.annotations.CreationTimestamp;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.AfterDomainEventPublication;

import javax.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Getter
@Setter(AccessLevel.PACKAGE)
@Table(name = "bitcoin_chain_info")
public class BitcoinChainInfo extends AbstractAggregateRoot<BitcoinChainInfo> implements AggregateRoot<BitcoinChainInfo, BitcoinChainInfo.BitcoinChainInfoId> {

    private final BitcoinChainInfoId id;

    @CreationTimestamp
    private Instant createdAt;

    private final String chain;
    private final long blocks;
    private final long headers;
    private final Sha256Hash bestBlockHash;
    private final String difficulty;
    private final String verificationProgress;
    private final String chainWork;

    BitcoinChainInfo(String chain,
                     long blocks,
                     long headers,
                     Sha256Hash bestBlockHash,
                     String difficulty,
                     String verificationProgress,
                     String chainWork) {
        this.id = BitcoinChainInfoId.of(UUID.randomUUID().toString());
        this.chain = chain;
        this.blocks = blocks;
        this.headers = headers;
        this.bestBlockHash = bestBlockHash;
        this.difficulty = difficulty;
        this.verificationProgress = verificationProgress;
        this.chainWork = chainWork;

        registerEvent(new BitcoinChainInfoCreatedEvent(this.getId()));
    }

    @AfterDomainEventPublication
    void afterDomainEventPublication() {
        log.trace("AfterDomainEventPublication");
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("createdAt", createdAt)
                .add("chain", chain)
                .add("blocks", blocks)
                .add("bestBlockHash", bestBlockHash)
                .toString();
    }

    @Value(staticConstructor = "of")
    public static class BitcoinChainInfoId implements Identifier {
        public static BitcoinChainInfoId create() {
            return BitcoinChainInfoId.of(UUID.randomUUID().toString());
        }

        @NonNull
        String id;
    }

    @Value(staticConstructor = "of")
    public static class BitcoinChainInfoCreatedEvent {

        @NonNull
        BitcoinChainInfoId domainId;

        public String toString() {
            return "BitcoinChainInfoCreatedEvent";
        }
    }
}
