package org.tbk.spring.lnurl.security.wallet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.auth.LinkingKey;
import org.tbk.lnurl.auth.Signature;
import org.tbk.lnurl.simple.auth.SimpleK1;
import org.tbk.lnurl.simple.auth.SimpleLinkingKey;
import org.tbk.lnurl.simple.auth.SimpleSignature;
import org.tbk.spring.lnurl.security.LnurlAuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Slf4j
public class LnurlAuthWalletAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private static final LnurlAuthWalletAuthenticationFailureHandler failureHandler = new LnurlAuthWalletAuthenticationFailureHandler();
    private static final LnurlAuthWalletAuthenticationSuccessHandler successHandler = new LnurlAuthWalletAuthenticationSuccessHandler();

    public static final String LNURL_AUTH_K1_KEY = "k1";
    public static final String LNURL_AUTH_SIG_KEY = "sig";
    public static final String LNURL_AUTH_KEY_KEY = "key";

    public LnurlAuthWalletAuthenticationFilter(String pathRequestPattern) {
        this(new AntPathRequestMatcher(pathRequestPattern, HttpMethod.GET.name()));
    }

    protected LnurlAuthWalletAuthenticationFilter(AntPathRequestMatcher pathRequestPattern) {
        super(pathRequestPattern);

        this.setAuthenticationFailureHandler(failureHandler);
        this.setAuthenticationSuccessHandler(successHandler);
        this.setAllowSessionCreation(false);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!request.getMethod().equals(HttpMethod.GET.name())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        LnurlAuthWalletToken authRequest = buildToken(request);

        if (log.isDebugEnabled()) {
            log.debug("got lnurl-auth wallet authentication request for k1 '{}'", authRequest.getK1().toHex());
        }

        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);
    }

    private LnurlAuthWalletToken buildToken(HttpServletRequest request) {
        try {
            K1 k1 = obtainK1(request).orElseThrow(() -> new LnurlAuthenticationException("'k1' is missing or invalid."));
            Signature sig = obtainSig(request).orElseThrow(() -> new LnurlAuthenticationException("'sig' is missing or invalid."));
            LinkingKey key = obtainKey(request).orElseThrow(() -> new LnurlAuthenticationException("'key' is missing or invalid."));

            return new LnurlAuthWalletToken(k1, sig, key);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new LnurlAuthenticationException("Cannot build wallet token from request", e);
        }
    }

    protected Optional<K1> obtainK1(HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(LNURL_AUTH_K1_KEY))
                .map(SimpleK1::fromHex);
    }

    protected Optional<Signature> obtainSig(HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(LNURL_AUTH_SIG_KEY))
                .map(SimpleSignature::fromHex);
    }

    protected Optional<LinkingKey> obtainKey(HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(LNURL_AUTH_KEY_KEY))
                .map(SimpleLinkingKey::fromHexStrict);
    }

    /**
     * Provided so that subclasses may configure what is put into the authentication
     * request's details property.
     *
     * @param request     that an authentication request is being created for
     * @param authRequest the authentication request object that should have its details
     *                    set
     */
    protected void setDetails(HttpServletRequest request, LnurlAuthWalletToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }
}