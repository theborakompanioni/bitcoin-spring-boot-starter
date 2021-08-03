package org.tbk.lnurl.auth;

import java.util.Optional;

public interface LnurlAuthPairingService {

    boolean pairK1WithLinkingKey(K1 k1, LinkingKey linkingKey);

    Optional<LinkingKey> findPairedLinkingKeyByK1(K1 k1);
}
