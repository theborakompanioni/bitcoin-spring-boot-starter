package org.tbk.lnurl.auth;

import java.util.Optional;

public interface LnurlAuthPairingService {

    Optional<LinkingKey> findPairedLinkingKeyByK1(K1 k1);

    boolean pairK1WithLinkingKey(K1 k1, LinkingKey linkingKey);
}
