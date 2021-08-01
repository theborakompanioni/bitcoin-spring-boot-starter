package org.tbk.lnurl.auth;

import org.tbk.lnurl.K1;

import java.util.Optional;

public interface LnurlAuthPairingService {

    Optional<byte[]> findPairedLinkingKeyByK1(K1 k1);

    void pairK1WithLinkingKey(K1 k1, byte[] linkingKey);
}
