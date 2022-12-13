package org.tbk.lightning.lnurl.example.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.tbk.lnurl.auth.LinkingKey;

import java.util.Optional;

interface AuthLinkingKeys extends CrudRepository<AuthLinkingKey, AuthLinkingKey.LinkingKeyId>,
        PagingAndSortingRepository<AuthLinkingKey, AuthLinkingKey.LinkingKeyId> {

    default Optional<AuthLinkingKey> findByLinkingKey(LinkingKey linkingKey) {
        return this.findByLinkingKey(linkingKey.toHex());
    }

    Optional<AuthLinkingKey> findByLinkingKey(@Param("linkingKey") String linkingKey);
}
