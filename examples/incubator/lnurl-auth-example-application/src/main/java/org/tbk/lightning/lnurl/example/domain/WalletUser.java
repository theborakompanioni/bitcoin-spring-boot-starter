package org.tbk.lightning.lnurl.example.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.auth.LinkingKey;
import org.tbk.lnurl.simple.auth.SimpleLinkingKey;

import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Version;
import java.time.Instant;
import java.util.*;

import static java.util.Objects.requireNonNull;

@Slf4j
@Getter
@Setter(AccessLevel.PACKAGE)
@Table(name = "lnurl_auth_wallet_user")
public class WalletUser extends AbstractAggregateRoot<WalletUser> implements AggregateRoot<WalletUser, WalletUser.WalletUserId> {

    private final WalletUserId id;

    private final long createdAt;

    @JsonIgnore
    @Version
    private Long version;

    private String name;

    private Long lastSuccessfulAuthAt;

    @JoinColumn(name = "lnurl_auth_wallet_user_id")
    private final List<AuthLinkingKey> linkingKeys = new ArrayList<>();

    WalletUser(AuthLinkingKey linkingKey) {
        this.id = WalletUserId.create();
        this.createdAt = Instant.now().toEpochMilli();
        this.name = "anon";

        linkingKeys.add(requireNonNull(linkingKey));

        registerEvent(new WalletUserCreatedEvent(this.id));
    }

    @AfterDomainEventPublication
    void afterDomainEventPublication() {
        log.trace("AfterDomainEventPublication");
    }

    WalletUser pair(LinkingKey linkingKey, K1 k1) {
        this.lastSuccessfulAuthAt = Instant.now().toEpochMilli();

        linkingKeys.stream()
                .filter(it -> it.getLinkingKey().equals(linkingKey.toHex()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Given 'linkingKey' not found."))
                .markUsedK1(k1);

        registerEvent(new WalletUserLoginSuccessfulEvent(this.id));

        return this;
    }

    public Optional<LinkingKey> getLinkingKeyForLeastRecentlyUsedK1(K1 k1) {
        String k1hex = k1.toHex();
        return linkingKeys.stream()
                .filter(it -> k1hex.equals(it.getLeastRecentlyUsedK1()))
                .map(AuthLinkingKey::getLinkingKey)
                .findFirst()
                .map(SimpleLinkingKey::fromHex);
    }

    @Value(staticConstructor = "of")
    public static class WalletUserId implements Identifier {
        public static WalletUserId create() {
            return WalletUserId.of(UUID.randomUUID().toString());
        }

        @NonNull
        String id;
    }

    @Value(staticConstructor = "of")
    public static class WalletUserCreatedEvent {

        @NonNull
        WalletUser.WalletUserId domainId;

        public String toString() {
            return "WalletUserCreatedEvent";
        }
    }

    @Value(staticConstructor = "of")
    public static class WalletUserLoginSuccessfulEvent {

        @NonNull
        WalletUser.WalletUserId domainId;

        public String toString() {
            return "WalletUserLoginSuccessfulEvent";
        }
    }
}
