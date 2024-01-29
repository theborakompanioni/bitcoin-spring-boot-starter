package org.tbk.lightning.lnurl.example.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.auth.LinkingKey;
import org.tbk.lnurl.simple.auth.SimpleLinkingKey;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    private Long accountDisabledAt;

    private Long accountLockedAt;

    private Long accountExpiredAt;

    private Long credentialsExpiredAt;

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
        super.clearDomainEvents();
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

    public boolean isAccountEnabled(Instant now) {
        return !isAccountDisabled(now);
    }

    private boolean isAccountDisabled(Instant now) {
        return accountDisabledAt != null && accountDisabledAt <= now.toEpochMilli();
    }

    public boolean isAccountLocked(Instant now) {
        return accountLockedAt != null && accountLockedAt <= now.toEpochMilli();
    }

    public boolean isAccountExpired(Instant now) {
        return accountExpiredAt != null && accountExpiredAt <= now.toEpochMilli();
    }

    public boolean isCredentialsExpired(Instant now) {
        return credentialsExpiredAt != null && credentialsExpiredAt <= now.toEpochMilli();
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
