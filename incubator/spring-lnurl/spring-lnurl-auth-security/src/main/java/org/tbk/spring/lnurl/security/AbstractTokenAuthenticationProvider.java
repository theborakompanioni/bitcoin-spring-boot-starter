package org.tbk.spring.lnurl.security;

import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.util.Assert;
import org.tbk.lnurl.auth.LinkingKey;

/**
 * Best parts copied from {@link AbstractUserDetailsAuthenticationProvider}
 * and applied to lnurl-auth token authentication.
 */
public abstract class AbstractTokenAuthenticationProvider implements AuthenticationProvider {

    private UserDetailsChecker authenticationChecks = new AccountStatusUserDetailsChecker();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isTrue(supports(authentication.getClass()), "Unsupported authentication class");

        LinkingKey linkingKey = retrieveLinkingKey(authentication);

        Assert.notNull(linkingKey, "linkingKey returned null - a violation of the interface contract");

        UserDetails userDetails = retrieveUser(linkingKey, authentication);

        Assert.notNull(userDetails, "retrieveUser returned null - a violation of the interface contract");

        getAuthenticationChecks().check(userDetails);

        return createSuccessAuthentication(linkingKey, authentication, userDetails);
    }

    protected abstract LinkingKey retrieveLinkingKey(Authentication authentication) throws AuthenticationException;

    protected abstract UserDetails retrieveUser(LinkingKey linkingKey, Authentication authentication) throws AuthenticationException;

    protected abstract Authentication createSuccessAuthentication(LinkingKey linkingKey, Authentication authentication, UserDetails user);

    protected UserDetailsChecker getAuthenticationChecks() {
        return this.authenticationChecks;
    }

    public void setAuthenticationChecks(UserDetailsChecker authenticationChecks) {
        this.authenticationChecks = authenticationChecks;
    }
}