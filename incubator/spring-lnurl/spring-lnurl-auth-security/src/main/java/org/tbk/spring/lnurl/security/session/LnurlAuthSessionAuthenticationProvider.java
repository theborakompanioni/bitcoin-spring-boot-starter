package org.tbk.spring.lnurl.security.session;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.tbk.lnurl.auth.LinkingKey;
import org.tbk.lnurl.auth.LnurlAuthPairingService;
import org.tbk.spring.lnurl.security.AbstractTokenAuthenticationProvider;
import org.tbk.spring.lnurl.security.LnurlAuthenticationException;

@RequiredArgsConstructor
public class LnurlAuthSessionAuthenticationProvider extends AbstractTokenAuthenticationProvider {

    @NonNull
    private final LnurlAuthPairingService lnurlAuthSecurityService;

    @NonNull
    private final UserDetailsService userDetailsService;

    @Override
    public boolean supports(Class<?> authentication) {
        return LnurlAuthSessionToken.class.isAssignableFrom(authentication);
    }

    @Override
    protected LinkingKey retrieveLinkingKey(Authentication authentication) throws AuthenticationException {
        LnurlAuthSessionToken auth = (LnurlAuthSessionToken) authentication;

        if (auth.isAuthenticated()) {
            throw new LnurlAuthenticationException("Already authenticated.");
        }

        return lnurlAuthSecurityService.findPairedLinkingKeyByK1(auth.getK1())
                .orElseThrow(() -> new LnurlAuthenticationException("Cannot migrate session."));
    }

    @Override
    protected UserDetails retrieveUser(LinkingKey linkingKey, Authentication authentication) throws AuthenticationException {
        return userDetailsService.loadUserByUsername(linkingKey.toHex());
    }

    @Override
    protected Authentication createSuccessAuthentication(LinkingKey linkingKey, Authentication authentication, UserDetails user) {
        LnurlAuthSessionToken auth = (LnurlAuthSessionToken) authentication;

        LnurlAuthSessionToken newAuth = new LnurlAuthSessionToken(auth.getK1(), linkingKey, user.getAuthorities());
        newAuth.setDetails(user);

        return newAuth;
    }
}
