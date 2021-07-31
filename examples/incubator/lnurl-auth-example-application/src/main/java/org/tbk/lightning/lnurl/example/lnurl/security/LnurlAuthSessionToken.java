package org.tbk.lightning.lnurl.example.lnurl.security;

import fr.acinq.secp256k1.Hex;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;
import org.tbk.lnurl.K1;

import java.util.Arrays;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

public class LnurlAuthSessionToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 1L;

    private final K1 k1;

    private byte[] linkingKey;

    public LnurlAuthSessionToken(K1 k1) {
        super(null);
        this.k1 = requireNonNull(k1);
        setAuthenticated(false);
    }

    public LnurlAuthSessionToken(K1 k1, byte[] linkingKey, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.k1 = requireNonNull(k1);
        this.linkingKey = Arrays.copyOf(linkingKey, linkingKey.length);
        super.setAuthenticated(true); // must use super, as we override
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        if (linkingKey != null) {
            return Hex.encode(this.linkingKey);
        } else {
            return k1.getHex();
        }
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated, "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);
    }

    public K1 getK1() {
        return k1;
    }

    public byte[] getLinkingKey() {
        return linkingKey == null ? null : Arrays.copyOf(linkingKey, linkingKey.length);
    }
}