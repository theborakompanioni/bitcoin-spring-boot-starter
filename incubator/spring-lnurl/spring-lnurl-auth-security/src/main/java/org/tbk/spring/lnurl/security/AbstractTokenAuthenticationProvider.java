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

import static java.util.Objects.requireNonNull;

/**
 * Best parts copied from {@link AbstractUserDetailsAuthenticationProvider}
 * and applied to lnurl-auth token authentication.
 */
public abstract class AbstractTokenAuthenticationProvider implements AuthenticationProvider {

    private UserDetailsChecker authenticationChecks = new AccountStatusUserDetailsChecker();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isTrue(supports(authentication.getClass()), "Unsupported authentication class");

        UserDetails userDetails = retrieveUser(authentication);

        Assert.notNull(userDetails, "retrieveUser returned null - a violation of the interface contract");

        getAuthenticationChecks().check(userDetails);

        return createSuccessAuthentication(authentication, userDetails);
    }

    protected abstract UserDetails retrieveUser(Authentication authentication) throws AuthenticationException;

    protected abstract Authentication createSuccessAuthentication(Authentication authentication, UserDetails user);

    protected UserDetailsChecker getAuthenticationChecks() {
        return this.authenticationChecks;
    }

    public void setAuthenticationChecks(UserDetailsChecker authenticationChecks) {
        this.authenticationChecks = requireNonNull(authenticationChecks);
    }
}