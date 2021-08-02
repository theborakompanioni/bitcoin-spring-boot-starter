package org.tbk.spring.lnurl.security.wallet;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;
import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.auth.LinkingKey;
import org.tbk.lnurl.auth.Signature;

import java.util.Collection;

import static java.util.Objects.requireNonNull;

public class LnurlAuthWalletToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 1L;

    @Getter
    private final K1 k1;

    @Getter
    private final Signature signature;

    @Getter
    private final LinkingKey linkingKey;

    public LnurlAuthWalletToken(K1 k1, Signature signature, LinkingKey linkingKey) {
        super(null);
        this.k1 = requireNonNull(k1);
        this.signature = requireNonNull(signature);
        this.linkingKey = requireNonNull(linkingKey);
        setAuthenticated(false);
    }

    public LnurlAuthWalletToken(K1 k1, Signature signature, LinkingKey linkingKey,
                                Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.k1 = requireNonNull(k1);
        this.signature = requireNonNull(signature);
        this.linkingKey = requireNonNull(linkingKey);
        super.setAuthenticated(true); // must use super, as we override
    }

    @Override
    public Object getCredentials() {
        // todo: really needed? why not just return ""?
        return this.k1.toHex() + ":" + this.signature.toHex();
    }

    @Override
    public Object getPrincipal() {
        return this.linkingKey.toHex();
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated, "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);
    }
}