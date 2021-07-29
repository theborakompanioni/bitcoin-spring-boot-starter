package org.tbk.lightning.lnurl.example.domain;

import org.jmolecules.spring.AssociationResolver;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.tbk.lightning.lnurl.example.domain.WalletUser.WalletUserId;

import java.util.Optional;

interface WalletUsers extends PagingAndSortingRepository<WalletUser, WalletUserId>, AssociationResolver<WalletUser, WalletUserId> {

    default Optional<WalletUser> findByLinkingKey(LinkingKey linkingKey) {
        return this.findByLinkingKey(linkingKey.getLinkingKey());
    }

    @Query(value = "SELECT wu.* FROM lnurl_auth_wallet_user wu "
            + "INNER JOIN lnurl_auth_linking_key lk ON lk.lnurl_auth_wallet_user_id = wu.id AND lk.linking_key = :linkingKey "
            + "LIMIT 1", nativeQuery = true)
    Optional<WalletUser> findByLinkingKey(@Param("linkingKey") String linkingKey);
}
