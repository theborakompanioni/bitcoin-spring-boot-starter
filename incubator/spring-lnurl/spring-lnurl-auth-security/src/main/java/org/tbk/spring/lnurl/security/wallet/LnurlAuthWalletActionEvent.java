package org.tbk.spring.lnurl.security.wallet;

import org.springframework.context.ApplicationEvent;
import org.tbk.lnurl.auth.LnurlAuth;

import java.util.Optional;

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

    /**
     * @deprecated Use {@link #getAuthentication()} instead
     */
    @Deprecated(since = "0.13.0", forRemoval = true)
    public LnurlAuth getLnurlAuth() {
        return authentication.getAuth();
    }

    /**
     * @deprecated Use {@link #getAuthentication()} instead
     */
    @Deprecated(since = "0.13.0", forRemoval = true)
    public Optional<LnurlAuth.Action> getAction() {
        return getLnurlAuth().getAction();
    }
}
