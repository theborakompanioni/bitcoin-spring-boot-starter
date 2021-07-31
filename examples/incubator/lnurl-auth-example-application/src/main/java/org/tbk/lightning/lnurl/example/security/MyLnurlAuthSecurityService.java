package org.tbk.lightning.lnurl.example.security;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.tbk.lightning.lnurl.example.domain.WalletUserService;
import org.tbk.lightning.lnurl.example.lnurl.security.LnurlAuthSecurityService;
import org.tbk.lnurl.K1;

import java.util.Optional;

@RequiredArgsConstructor
public class MyLnurlAuthSecurityService implements LnurlAuthSecurityService {

    @NonNull
    private final WalletUserService walletUserService;

    @Override
    @Transactional
    public Optional<byte[]> findLinkingKeyByPairedK1(K1 k1) {
        return walletUserService.findByLeastRecentlyUsedK1(k1)
                .flatMap(it -> it.getLinkingKeyForLeastRecentlyUsedK1(k1));
    }

    @Override
    @Transactional
    public void pairLinkingKeyWithK1(byte[] linkingKey, K1 k1) {
        walletUserService.pairLinkingKeyWithK1(linkingKey, k1);
    }
}
