package org.tbk.spring.lnurl.security.session;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;
import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.auth.LinkingKey;

import javax.annotation.Nullable;
import java.io.Serial;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

public final class LnurlAuthSessionToken extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private final K1 k1;

    @Nullable
    private Object principal;

    LnurlAuthSessionToken(K1 k1) {
        super(null);
        this.k1 = requireNonNull(k1);
        setAuthenticated(false);
    }

    LnurlAuthSessionToken(K1 k1, Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.k1 = requireNonNull(k1);
        this.principal = principal;
        super.setAuthenticated(true); // must use super, as we override
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        if (!this.isAuthenticated()) {
            throw new IllegalStateException("Cannot call method 'getPrincipal' on unauthenticated session token");
        }

        return principal;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated, "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);
    }
}
