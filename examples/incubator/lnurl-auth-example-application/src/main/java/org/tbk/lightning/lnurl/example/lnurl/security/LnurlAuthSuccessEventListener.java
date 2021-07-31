package org.tbk.lightning.lnurl.example.lnurl.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.tbk.lightning.lnurl.example.lnurl.security.session.LnurlAuthSessionToken;
import org.tbk.lightning.lnurl.example.lnurl.security.wallet.LnurlAuthWalletToken;

@Slf4j
@Component // todo: move bean creation to own auto config
public class LnurlAuthSuccessEventListener implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {

    @Override
    public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();

        if (authentication instanceof LnurlAuthWalletToken) {
            onSuccessfulAuthentication((LnurlAuthWalletToken) authentication);
        } else if (authentication instanceof LnurlAuthSessionToken) {
            onSuccessfulAuthentication((LnurlAuthSessionToken) authentication);
        }
    }

    private void onSuccessfulAuthentication(LnurlAuthWalletToken authentication) {
        log.info("Successful authenticated user: {}", authentication.getPrincipal());
    }

    private void onSuccessfulAuthentication(LnurlAuthSessionToken authentication) {
        log.info("Successful authenticated user: {}", authentication.getPrincipal());

    }
}
