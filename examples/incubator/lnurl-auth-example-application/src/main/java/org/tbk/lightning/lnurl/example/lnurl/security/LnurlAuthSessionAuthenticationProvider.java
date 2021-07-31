package org.tbk.lightning.lnurl.example.lnurl.security;

import fr.acinq.secp256k1.Hex;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.tbk.lightning.lnurl.example.domain.WalletUser;
import org.tbk.lightning.lnurl.example.domain.WalletUserService;
import org.tbk.lightning.lnurl.example.lnurl.K1Manager;

@RequiredArgsConstructor
public class LnurlAuthSessionAuthenticationProvider implements AuthenticationProvider {

    @NonNull
    private final WalletUserService walletUserService;

    @NonNull
    private final UserDetailsService userDetailsService;

    @Override
    public boolean supports(Class<?> authentication) {
        return LnurlAuthSessionToken.class.isAssignableFrom(authentication);
    }

    @Override
    @Transactional
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isTrue(supports(authentication.getClass()), "Unsupported authentication class");

        LnurlAuthSessionToken auth = (LnurlAuthSessionToken) authentication;
        if (auth.isAuthenticated()) {
            throw new LnurlAuthenticationException("Already authenticated.");
        }

        WalletUser walletUser = walletUserService.login(auth)
                .orElseThrow(() -> new LnurlAuthenticationException("Cannot migrate session."));

        byte[] linkingKey = walletUser.getLinkingKeyForLeastRecentlyUsedK1(auth.getK1())
                .orElseThrow(() -> new LnurlAuthenticationException("Cannot migrate session."));

        UserDetails userDetails = userDetailsService.loadUserByUsername(Hex.encode(linkingKey));
        LnurlAuthSessionToken newAuth = new LnurlAuthSessionToken(auth.getK1(), linkingKey, userDetails.getAuthorities());

        newAuth.setDetails(userDetails);

        return newAuth;
    }

}