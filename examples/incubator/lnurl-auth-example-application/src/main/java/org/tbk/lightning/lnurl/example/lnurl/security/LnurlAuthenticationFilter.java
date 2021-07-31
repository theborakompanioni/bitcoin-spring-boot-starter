package org.tbk.lightning.lnurl.example.lnurl.security;

import fr.acinq.secp256k1.Hex;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.tbk.lnurl.K1;
import org.tbk.lnurl.simple.SimpleK1;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Slf4j
public class LnurlAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private static final LnurlAuthenticationFailureHandler failureHandler = new LnurlAuthenticationFailureHandler();
    private static final LnurlAuthenticationSuccessHandler successHandler = new LnurlAuthenticationSuccessHandler();

    public static final String LNURL_AUTH_K1_KEY = "k1";
    public static final String LNURL_AUTH_SIG_KEY = "sig";
    public static final String LNURL_AUTH_KEY_KEY = "key";

    public LnurlAuthenticationFilter(String pathRequestPattern, AuthenticationManager authenticationManager) {
        super(new AntPathRequestMatcher(pathRequestPattern, HttpMethod.GET.name()), authenticationManager);

        this.setAuthenticationFailureHandler(failureHandler);
        this.setAuthenticationSuccessHandler(successHandler);
        this.setAllowSessionCreation(false); // session will only be created on login page
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!request.getMethod().equals(HttpMethod.GET.name())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        log.debug("got lnurl-auth request: {}", request.getRequestURI());

        LnurlAuthenticationToken authRequest = buildToken(request);

        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);
    }

    private LnurlAuthenticationToken buildToken(HttpServletRequest request) {
        try {
            K1 k1 = obtainK1(request).orElseThrow(() -> new LnurlAuthenticationException("'k1' is missing or invalid."));
            byte[] sig = obtainSig(request).orElseThrow(() -> new LnurlAuthenticationException("'sig' is missing or invalid."));
            byte[] key = obtainKey(request).orElseThrow(() -> new LnurlAuthenticationException("'key' is missing or invalid."));

            return new LnurlAuthenticationToken(k1, sig, key);
        } catch (IllegalArgumentException e) {
            throw new LnurlAuthenticationException("Authentication error: " + e.getMessage());
        }
    }

    protected Optional<K1> obtainK1(HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(LNURL_AUTH_K1_KEY))
                .map(SimpleK1::fromHex);
    }

    protected Optional<byte[]> obtainSig(HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(LNURL_AUTH_SIG_KEY))
                .map(Hex::decode);
    }

    protected Optional<byte[]> obtainKey(HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(LNURL_AUTH_KEY_KEY))
                .map(Hex::decode);
    }

    /**
     * Provided so that subclasses may configure what is put into the authentication
     * request's details property.
     *
     * @param request     that an authentication request is being created for
     * @param authRequest the authentication request object that should have its details
     *                    set
     */
    protected void setDetails(HttpServletRequest request, LnurlAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }
}