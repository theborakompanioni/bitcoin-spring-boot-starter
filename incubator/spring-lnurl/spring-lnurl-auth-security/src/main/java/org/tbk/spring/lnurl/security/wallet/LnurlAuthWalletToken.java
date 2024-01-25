package org.tbk.spring.lnurl.security.wallet;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;
import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.auth.LinkingKey;
import org.tbk.lnurl.auth.Signature;
import org.tbk.lnurl.auth.SignedLnurlAuth;

import java.io.Serial;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

public final class LnurlAuthWalletToken extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = 2L;

    @Getter
    private final SignedLnurlAuth auth;

    LnurlAuthWalletToken(SignedLnurlAuth auth) {
        super(null);
        this.auth = requireNonNull(auth);
        setAuthenticated(false);
    }

    LnurlAuthWalletToken(SignedLnurlAuth auth, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.auth = requireNonNull(auth);
        super.setAuthenticated(true); // must use super, as we override
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.getAuth().getLinkingKey().toHex();
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated, "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);
    }

    /**
     * @deprecated Use {@link #getAuth()} instead
     */
    @Deprecated(since = "0.13.0", forRemoval = true)
    public K1 getK1() {
        return auth.getK1();
    }

    /**
     * @deprecated Use {@link #getAuth()} instead
     */
    @Deprecated(since = "0.13.0", forRemoval = true)
    public Signature getSignature() {
        return auth.getSignature();
    }

    /**
     * @deprecated Use {@link #getAuth()} instead
     */
    @Deprecated(since = "0.13.0", forRemoval = true)
    public LinkingKey getLinkingKey() {
        return auth.getLinkingKey();
    }
}