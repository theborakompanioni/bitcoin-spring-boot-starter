package org.tbk.lightning.lnurl.example.lnurl.security;

import org.tbk.lnurl.K1;

import java.util.Optional;

public interface LnurlAuthSecurityService {

    Optional<byte[]> findLinkingKeyByPairedK1(K1 k1);

    void pairLinkingKeyWithK1(byte[] linkingKey, K1 k1);
}
