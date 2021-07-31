package org.tbk.lightning.lnurl.example.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.acinq.bitcoin.Crypto;
import fr.acinq.secp256k1.Hex;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.types.Entity;
import org.jmolecules.ddd.types.Identifier;
import org.tbk.lnurl.K1;
import scodec.bits.ByteVector;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Version;
import java.time.Instant;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
@Getter
@Setter(AccessLevel.PACKAGE)
@Table(name = "lnurl_auth_linking_key")
class LinkingKey implements Entity<WalletUser, LinkingKey.LinkingKeyId> {

    private final LinkingKeyId id;

    private final long createdAt;

    @JsonIgnore
    @Version
    private Long version;

    private final String linkingKey;

    @Column(name = "least_recently_used_k1")
    private String leastRecentlyUsedK1;

    LinkingKey(byte[] linkingKey) {
        this.id = LinkingKeyId.create();
        this.createdAt = Instant.now().toEpochMilli();
        this.linkingKey = Hex.encode(linkingKey);

        checkArgument(this.toPublicKey().isValid(), "Linking key must be a valid public key");
    }

    public Crypto.PublicKey toPublicKey() {
        return new Crypto.PublicKey(ByteVector.view(Hex.decode(linkingKey)));
    }

    public void markUsedK1(K1 k1) {
        this.leastRecentlyUsedK1 = k1.getHex();
    }

    @Value(staticConstructor = "of")
    public static class LinkingKeyId implements Identifier {
        public static LinkingKeyId create() {
            return LinkingKeyId.of(UUID.randomUUID().toString());
        }

        @NonNull
        String id;
    }
}
