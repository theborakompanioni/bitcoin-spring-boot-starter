package org.tbk.lightning.lnurl.example.domain;

import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.auth.LinkingKey;

import java.util.Optional;

public interface WalletUserService {

    Optional<WalletUser> findUser(LinkingKey linkingKey);

    WalletUser findUserOrCreateIfMissing(LinkingKey linkingKey);

    void pairLinkingKeyWithK1(LinkingKey linkingKey, K1 k1);

    Optional<WalletUser> findByLeastRecentlyUsedK1(K1 k1);
}
