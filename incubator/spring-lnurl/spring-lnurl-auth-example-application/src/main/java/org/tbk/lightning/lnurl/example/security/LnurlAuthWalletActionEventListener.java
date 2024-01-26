package org.tbk.lightning.lnurl.example.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.tbk.lnurl.auth.LnurlAuth;
import org.tbk.lnurl.auth.SignedLnurlAuth;
import org.tbk.spring.lnurl.security.wallet.LnurlAuthWalletActionEvent;

import java.net.URI;

@Slf4j
@Component
class LnurlAuthWalletActionEventListener implements ApplicationListener<LnurlAuthWalletActionEvent> {

    @Override
    public void onApplicationEvent(LnurlAuthWalletActionEvent event) {
        SignedLnurlAuth auth = event.getAuthentication().getAuth();
        URI callbackUrl = auth.toLnurl().toUri();
        String action = auth.getAction()
                .map(LnurlAuth.Action::getValue)
                .orElse("<empty>");

        log.info("Received lnurl-auth wallet action ({}) event: {}", action, callbackUrl);
    }
}
