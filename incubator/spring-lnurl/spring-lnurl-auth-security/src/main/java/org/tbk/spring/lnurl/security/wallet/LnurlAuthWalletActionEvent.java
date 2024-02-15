package org.tbk.spring.lnurl.security.wallet;

import org.springframework.context.ApplicationEvent;

import static java.util.Objects.requireNonNull;

/**
 * Application event which indicates an incoming lnurl-auth action from a wallet.
 * The event is only triggered if the request is valid and a {@link LnurlAuthWalletToken} is present.
 */
public final class LnurlAuthWalletActionEvent extends ApplicationEvent {

    private final LnurlAuthWalletToken authentication;

    LnurlAuthWalletActionEvent(Object source, LnurlAuthWalletToken authentication) {
        super(source);
        this.authentication = requireNonNull(authentication);
    }

    public LnurlAuthWalletToken getAuthentication() {
        return authentication;
    }
}
