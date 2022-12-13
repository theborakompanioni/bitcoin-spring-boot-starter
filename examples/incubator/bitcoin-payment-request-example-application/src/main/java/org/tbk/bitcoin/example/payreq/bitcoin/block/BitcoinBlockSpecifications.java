package org.tbk.bitcoin.example.payreq.bitcoin.block;

import jakarta.validation.constraints.NotNull;
import org.bitcoinj.core.Sha256Hash;
import org.springframework.data.jpa.domain.Specification;


final class BitcoinBlockSpecifications {

    private BitcoinBlockSpecifications() {
        throw new UnsupportedOperationException();
    }

    static @NotNull Specification<BitcoinBlock> byHash(@NotNull Sha256Hash hash) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("hash"), hash);
    }
}
