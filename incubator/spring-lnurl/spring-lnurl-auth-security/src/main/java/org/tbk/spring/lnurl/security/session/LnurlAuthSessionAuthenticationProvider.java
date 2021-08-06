package org.tbk.spring.lnurl.security.session;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.Assert;
import org.tbk.lnurl.auth.LinkingKey;
import org.tbk.lnurl.auth.LnurlAuthPairingService;
import org.tbk.spring.lnurl.security.LnurlAuthenticationException;

@RequiredArgsConstructor
public class LnurlAuthSessionAuthenticationProvider implements AuthenticationProvider {

    @NonNull
    private final LnurlAuthPairingService lnurlAuthSecurityService;

    @NonNull
    private final UserDetailsService userDetailsService;

    @Override
    public boolean supports(Class<?> authentication) {
        return LnurlAuthSessionToken.class.isAssignableFrom(authentication);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isTrue(supports(authentication.getClass()), "Unsupported authentication class");

        LnurlAuthSessionToken auth = (LnurlAuthSessionToken) authentication;
        return authenticateInternal(auth);
    }

    private Authentication authenticateInternal(LnurlAuthSessionToken auth) {
        if (auth.isAuthenticated()) {
            throw new LnurlAuthenticationException("Already authenticated.");
        }

        LinkingKey linkingKey = lnurlAuthSecurityService.findPairedLinkingKeyByK1(auth.getK1())
                .orElseThrow(() -> new LnurlAuthenticationException("Cannot migrate session."));

        UserDetails userDetails = userDetailsService.loadUserByUsername(linkingKey.toHex());
        LnurlAuthSessionToken newAuth = new LnurlAuthSessionToken(auth.getK1(), linkingKey, userDetails.getAuthorities());

        newAuth.setDetails(userDetails);

        return newAuth;
    }

}