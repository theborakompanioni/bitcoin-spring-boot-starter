package org.tbk.spring.lnurl.security.session;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.tbk.spring.lnurl.security.AbstractTokenAuthenticationProvider;
import org.tbk.spring.lnurl.security.LnurlAuthenticationException;
import org.tbk.spring.lnurl.security.userdetails.LnurlAuthUserPairingService;

@RequiredArgsConstructor
public class LnurlAuthSessionAuthenticationProvider extends AbstractTokenAuthenticationProvider {

    @NonNull
    private final LnurlAuthUserPairingService lnurlAuthUserPairingService;

    @Override
    public boolean supports(Class<?> authentication) {
        return LnurlAuthSessionToken.class.isAssignableFrom(authentication);
    }

    @Override
    protected UserDetails retrieveUser(Authentication authentication) throws AuthenticationException {
        LnurlAuthSessionToken auth = (LnurlAuthSessionToken) authentication;

        if (auth.isAuthenticated()) {
            throw new LnurlAuthenticationException("Already authenticated.");
        }

        return lnurlAuthUserPairingService.findPairedUserByK1(auth.getK1())
                .orElseThrow(() -> new LnurlAuthenticationException("Cannot migrate session."));
    }


    @Override
    protected Authentication createSuccessAuthentication(Authentication authentication, UserDetails user) {
        LnurlAuthSessionToken auth = (LnurlAuthSessionToken) authentication;

        LnurlAuthSessionToken newAuth = new LnurlAuthSessionToken(auth.getK1(), user.getUsername(), user.getAuthorities());
        newAuth.setDetails(user);

        return newAuth;
    }
}
