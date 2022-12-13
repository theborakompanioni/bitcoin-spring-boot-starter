package org.tbk.lightning.lnurl.example.domain;

import org.jmolecules.spring.AssociationResolver;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.tbk.lnurl.auth.K1;

import java.util.Optional;

interface WalletUsers extends CrudRepository<WalletUser, WalletUser.WalletUserId>,
        PagingAndSortingRepository<WalletUser, WalletUser.WalletUserId>,
        AssociationResolver<WalletUser, WalletUser.WalletUserId> {

    default Optional<WalletUser> findByLinkingKey(AuthLinkingKey linkingKey) {
        return this.findByLinkingKey(linkingKey.getLinkingKey());
    }

    @Query(value = "SELECT wu.* FROM lnurl_auth_wallet_user wu "
            + "INNER JOIN lnurl_auth_linking_key lk ON lk.lnurl_auth_wallet_user_id = wu.id AND lk.linking_key = :linkingKey "
            + "LIMIT 1", nativeQuery = true)
    Optional<WalletUser> findByLinkingKey(@Param("linkingKey") String linkingKey);

    default Optional<WalletUser> findByLeastRecentlyUsedK1(K1 k1) {
        return this.findByLeastRecentlyUsedK1(k1.toHex());
    }

    @Query(value = "SELECT wu.* FROM lnurl_auth_wallet_user wu "
            + "INNER JOIN lnurl_auth_linking_key lk ON lk.lnurl_auth_wallet_user_id = wu.id AND lk.least_recently_used_k1 = :k1 "
            + "LIMIT 1", nativeQuery = true)
    Optional<WalletUser> findByLeastRecentlyUsedK1(@Param("k1") String k1);
}
