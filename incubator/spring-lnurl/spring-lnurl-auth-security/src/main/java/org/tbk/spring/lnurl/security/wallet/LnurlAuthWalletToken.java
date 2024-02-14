package org.tbk.spring.lnurl.security.wallet;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;
import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.auth.LinkingKey;
import org.tbk.lnurl.auth.Signature;
import org.tbk.lnurl.auth.SignedLnurlAuth;

import javax.annotation.Nullable;
import java.io.Serial;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

public final class LnurlAuthWalletToken extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = 2L;

    @Getter
    private final SignedLnurlAuth auth;

    @Nullable
    private final Object principal;

    LnurlAuthWalletToken(SignedLnurlAuth auth) {
        super(null);
        this.auth = requireNonNull(auth);
        this.principal = null;
        setAuthenticated(false);
    }

    LnurlAuthWalletToken(SignedLnurlAuth auth, Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.auth = requireNonNull(auth);
        this.principal = requireNonNull(principal, "'principal' must be provided for an authenticated token");
        super.setAuthenticated(true); // must use super, as we override
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        if (!this.isAuthenticated()) {
            throw new IllegalStateException("Cannot call method 'getPrincipal' on unauthenticated wallet token");
        }

        return principal;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated, "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);
    }
}
