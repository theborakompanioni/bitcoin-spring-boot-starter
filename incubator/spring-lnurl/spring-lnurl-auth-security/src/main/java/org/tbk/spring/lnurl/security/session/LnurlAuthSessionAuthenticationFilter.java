package org.tbk.spring.lnurl.security.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.simple.auth.SimpleK1;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Slf4j
public class LnurlAuthSessionAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public LnurlAuthSessionAuthenticationFilter(String pathRequestPattern) {
        this(new AntPathRequestMatcher(pathRequestPattern, HttpMethod.GET.name()));
    }

    protected LnurlAuthSessionAuthenticationFilter(AntPathRequestMatcher pathRequestPattern) {
        super(pathRequestPattern);
        this.setAllowSessionCreation(false); // session will only be created on login page
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        Optional<K1> k1 = Optional.of(request)
                .map(HttpServletRequest::getSession)
                .map(it -> (String) it.getAttribute("k1"))
                .map(SimpleK1::fromHex);

        if (log.isDebugEnabled()) {
            log.debug("got lnurl-auth session migration request for k1 '{}'", k1.map(K1::toHex).orElse(null));
        }

        if (k1.isEmpty()) {
            return null; // indicate we cannot attempt session migration and won't handle the authentication.
        }

        LnurlAuthSessionToken lnurlAuthSessionToken = new LnurlAuthSessionToken(k1.get());

        setDetails(request, lnurlAuthSessionToken);

        return this.getAuthenticationManager().authenticate(lnurlAuthSessionToken);
    }

    protected void setDetails(HttpServletRequest request, LnurlAuthSessionToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }
}