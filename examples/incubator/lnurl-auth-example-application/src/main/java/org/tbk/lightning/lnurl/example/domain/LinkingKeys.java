package org.tbk.lightning.lnurl.example.domain;

import fr.acinq.secp256k1.Hex;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface LinkingKeys extends PagingAndSortingRepository<LinkingKey, LinkingKey.LinkingKeyId> {

    default Optional<LinkingKey> findByLinkingKey(byte[] linkingKey) {
        return this.findByLinkingKey(Hex.encode(linkingKey));
    }

    Optional<LinkingKey> findByLinkingKey(@Param("linkingKey") String linkingKey);
}
