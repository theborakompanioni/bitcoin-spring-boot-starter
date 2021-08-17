package org.tbk.lightning.lnurl.example.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.tbk.lnurl.auth.LnurlAuth;
import org.tbk.spring.lnurl.security.wallet.LnurlAuthWalletActionEvent;

import java.net.URI;

@Slf4j
@Component
class LnurlAuthWalletActionEventListener implements ApplicationListener<LnurlAuthWalletActionEvent> {

    @Override
    public void onApplicationEvent(LnurlAuthWalletActionEvent event) {
        LnurlAuth.Action action = event.getAction().orElse(null);

        URI callbackUrl = event.getLnurlAuth().toLnurl().toUri();
        if (action != null) {
            switch (action) {
                case LOGIN:
                    log.info("Got LOGIN action: {}", callbackUrl);
                    break;
                case REGISTER:
                    log.info("Got REGISTER action: {}", callbackUrl);
                    break;
                case LINK:
                    log.info("Got LINK action: {}", callbackUrl);
                    break;
                case AUTH:
                    log.info("Got AUTH action: {}", callbackUrl);
                    break;
            }
        }
    }
}
