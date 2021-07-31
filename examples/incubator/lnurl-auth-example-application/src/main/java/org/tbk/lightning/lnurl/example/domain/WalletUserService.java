package org.tbk.lightning.lnurl.example.domain;

import org.tbk.lnurl.K1;

import java.util.Optional;

public interface WalletUserService {

    Optional<WalletUser> findUser(byte[] linkingKey);

    WalletUser findUserOrCreateIfMissing(byte[] linkingKey);

    void pairLinkingKeyWithK1(byte[] linkingKey, K1 k1);

    Optional<WalletUser> findByLeastRecentlyUsedK1(K1 k1);
}
