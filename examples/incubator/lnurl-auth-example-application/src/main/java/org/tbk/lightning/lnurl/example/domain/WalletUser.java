package org.tbk.lightning.lnurl.example.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Association;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.AfterDomainEventPublication;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

    @JoinColumn(name = "lnurl_auth_wallet_user_id")
    private final List<LinkingKey> linkingKeys = new ArrayList<>();

    WalletUser(LinkingKey linkingKey) {
        this.id = WalletUserId.create();
        this.createdAt = Instant.now().toEpochMilli();
        this.name = "anon";

        //this.linkingKey = requireNonNull(linkingKey);
        linkingKeys.add(requireNonNull(linkingKey));

        registerEvent(new WalletUserCreatedEvent(this.id));
    }

    @AfterDomainEventPublication
    void afterDomainEventPublication() {
        log.trace("AfterDomainEventPublication");
    }

    WalletUser login() {
        this.lastSuccessfulAuthAt = Instant.now().toEpochMilli();

        registerEvent(new WalletUserLoginSuccessfulEvent(this.id));

        return this;
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
