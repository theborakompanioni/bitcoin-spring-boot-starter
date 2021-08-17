package org.tbk.lightning.lnurl.example.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.tbk.lnurl.auth.LnurlAuth;
import org.tbk.lnurl.auth.SignedLnurlAuth;
import org.tbk.spring.lnurl.security.session.LnurlAuthSessionToken;
import org.tbk.spring.lnurl.security.wallet.LnurlAuthWalletActionEvent;
import org.tbk.spring.lnurl.security.wallet.LnurlAuthWalletToken;

@Slf4j
@Component
class LnurlAuthSuccessEventListener implements ApplicationListener<AuthenticationSuccessEvent> {

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();

        if (authentication instanceof LnurlAuthSessionToken) {
            onSuccessfulAuthentication((LnurlAuthSessionToken) authentication);
        } else if (authentication instanceof LnurlAuthWalletToken) {
            onSuccessfulAuthentication((LnurlAuthWalletToken) authentication);
        }
    }

    private void onSuccessfulAuthentication(LnurlAuthSessionToken authentication) {
        log.debug("Successful lnurl-auth authenticated session user: {}", authentication.getPrincipal());
    }

    private void onSuccessfulAuthentication(LnurlAuthWalletToken authentication) {
        log.debug("Successful lnurl-auth authenticated wallet user: {}", authentication.getPrincipal());
    }
}
