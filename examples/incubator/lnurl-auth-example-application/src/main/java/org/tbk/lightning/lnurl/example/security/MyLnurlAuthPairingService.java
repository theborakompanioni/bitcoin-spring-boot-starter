package org.tbk.lightning.lnurl.example.security;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.tbk.lightning.lnurl.example.domain.WalletUserService;
import org.tbk.lnurl.K1;
import org.tbk.lnurl.auth.LnurlAuthPairingService;

import java.util.Optional;

@RequiredArgsConstructor
public class MyLnurlAuthPairingService implements LnurlAuthPairingService {

    @NonNull
    private final WalletUserService walletUserService;

    @Override
    @Transactional
    public Optional<byte[]> findPairedLinkingKeyByK1(K1 k1) {
        return walletUserService.findByLeastRecentlyUsedK1(k1)
                .flatMap(it -> it.getLinkingKeyForLeastRecentlyUsedK1(k1));
    }

    @Override
    @Transactional
    public void pairK1WithLinkingKey(K1 k1, byte[] linkingKey) {
        walletUserService.pairLinkingKeyWithK1(linkingKey, k1);
    }
}
