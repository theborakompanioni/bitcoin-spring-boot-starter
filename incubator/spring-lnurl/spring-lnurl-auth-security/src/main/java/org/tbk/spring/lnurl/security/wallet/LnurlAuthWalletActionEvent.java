package org.tbk.spring.lnurl.security.wallet;

import org.springframework.context.ApplicationEvent;
import org.tbk.lnurl.auth.LnurlAuth;
import org.tbk.lnurl.auth.SignedLnurlAuth;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Application event which indicates an incoming lnurl-auth action from a wallet.
 * The event is only triggered if the request is valid and a {@link LnurlAuthWalletToken} is present.
 */
public final class LnurlAuthWalletActionEvent extends ApplicationEvent {

    private final LnurlAuthWalletToken authentication;
    private final SignedLnurlAuth lnurlAuth;

    LnurlAuthWalletActionEvent(Object source, LnurlAuthWalletToken authentication, SignedLnurlAuth lnurlAuth) {
        super(source);
        this.authentication = requireNonNull(authentication);
        this.lnurlAuth = requireNonNull(lnurlAuth);
    }

    public LnurlAuthWalletToken getAuthentication() {
        return authentication;
    }

    public LnurlAuth getLnurlAuth() {
        return lnurlAuth;
    }

    public Optional<LnurlAuth.Action> getAction() {
        return lnurlAuth.getAction();
    }
}
