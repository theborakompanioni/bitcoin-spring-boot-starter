package org.tbk.lightning.lnurl.example.security;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.tbk.lightning.lnurl.example.domain.WalletUser;
import org.tbk.lightning.lnurl.example.domain.WalletUserService;
import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.auth.SignedLnurlAuth;
import org.tbk.spring.lnurl.security.userdetails.LnurlAuthUserPairingService;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
public class LnurlAuthUserPairingServiceImpl implements LnurlAuthUserPairingService {

    @NonNull
    private final WalletUserService walletUserService;

    @Override
    @Transactional
    public UserDetails pairUserWithK1(SignedLnurlAuth auth) {
        WalletUser user = walletUserService.findUserOrCreateIfMissing(auth.getLinkingKey());
        walletUserService.pairLinkingKeyWithK1(auth.getLinkingKey(), auth.getK1());

        return walletUserToDetails(user);
    }

    @Override
    public Optional<UserDetails> findPairedUserByK1(K1 k1) {
        return walletUserService.findByLeastRecentlyUsedK1(k1)
                .map(LnurlAuthUserPairingServiceImpl::walletUserToDetails);
    }

    private static UserDetails walletUserToDetails(WalletUser user) {
        Instant now = Instant.now();

        return User.builder()
                .username(user.getName())
                .password("") // password must not be null -_-
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .disabled(!user.isAccountEnabled(now))
                .credentialsExpired(user.isCredentialsExpired(now))
                .accountExpired(user.isAccountExpired(now))
                .accountLocked(user.isAccountLocked(now))
                .build();
    }
}
