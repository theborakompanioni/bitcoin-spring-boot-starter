package org.tbk.lightning.lnurl.example.security;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.tbk.lightning.lnurl.example.domain.WalletUserService;
import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.auth.LinkingKey;
import org.tbk.lnurl.auth.LnurlAuthPairingService;

import java.util.Optional;

@RequiredArgsConstructor
public class LnurlAuthPairingServiceImpl implements LnurlAuthPairingService {

    @NonNull
    private final WalletUserService walletUserService;

    @Override
    @Transactional
    public Optional<LinkingKey> findPairedLinkingKeyByK1(K1 k1) {
        return walletUserService.findByLeastRecentlyUsedK1(k1)
                .flatMap(it -> it.getLinkingKeyForLeastRecentlyUsedK1(k1));
    }

    @Override
    @Transactional
    public boolean pairK1WithLinkingKey(K1 k1, LinkingKey linkingKey) {
        walletUserService.findUserOrCreateIfMissing(linkingKey);
        walletUserService.pairLinkingKeyWithK1(linkingKey, k1);
        return true;
    }
}
