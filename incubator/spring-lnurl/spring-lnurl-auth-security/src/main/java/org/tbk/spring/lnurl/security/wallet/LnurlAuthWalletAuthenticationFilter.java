package org.tbk.spring.lnurl.security.wallet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.util.ForwardedHeaderUtils;
import org.tbk.lnurl.auth.SignedLnurlAuth;
import org.tbk.lnurl.simple.auth.SimpleSignedLnurlAuth;
import org.tbk.spring.lnurl.security.LnurlAuthenticationException;

import java.io.IOException;
import java.net.URI;

@Slf4j
public class LnurlAuthWalletAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private static final LnurlAuthWalletAuthenticationFailureHandler failureHandler = new LnurlAuthWalletAuthenticationFailureHandler();
    private static final LnurlAuthWalletAuthenticationSuccessHandler successHandler = new LnurlAuthWalletAuthenticationSuccessHandler();

    private static final String successBody = """
            {
              "status": "OK"
            }
            """;
    private static final String errorBody = """
            {
              "status": "ERROR",
              "reason": "Request could not be authenticated."
            }
            """;

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
    public final Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!request.getMethod().equals(HttpMethod.GET.name())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        LnurlAuthWalletToken authRequest = buildToken(request);

        if (log.isDebugEnabled()) {
            SignedLnurlAuth auth = authRequest.getAuth();
            log.debug("got lnurl-auth wallet authentication request for k1 '{}' from '{}'", auth.getK1().toHex(), auth.getLinkingKey().toHex());
        }

        setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);

        if (authResult instanceof LnurlAuthWalletToken walletToken && this.eventPublisher != null) {
            this.eventPublisher.publishEvent(new LnurlAuthWalletActionEvent(this, walletToken));
        }

        if (!response.isCommitted()) {
            writeSuccessBody(request, response);
        }
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        super.unsuccessfulAuthentication(request, response, failed);

        if (!response.isCommitted()) {
            writeErrorBody(request, response);
        }
    }

    @SuppressFBWarnings("XSS_SERVLET") // false positive - a hardcoded value is written
    protected void writeSuccessBody(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(successBody);
    }

    @SuppressFBWarnings("XSS_SERVLET") // false positive - a hardcoded value is written
    protected void writeErrorBody(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(errorBody);
    }

    protected LnurlAuthWalletToken buildToken(HttpServletRequest request) {
        try {
            var sshr = request instanceof ServletServerHttpRequest ? (ServletServerHttpRequest) request : new ServletServerHttpRequest(request);
            URI requestUri = ForwardedHeaderUtils.adaptFromForwardedHeaders(sshr.getURI(), sshr.getHeaders()).build().toUri();
            SignedLnurlAuth signedLnurlAuth = SimpleSignedLnurlAuth.from(requestUri);
            return new LnurlAuthWalletToken(signedLnurlAuth);
        } catch (LnurlAuthenticationException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new LnurlAuthenticationException(e.getMessage(), e);
        } catch (Exception e) {
            throw new LnurlAuthenticationException("Cannot build wallet token from request", e);
        }
    }

    protected void setDetails(HttpServletRequest request, LnurlAuthWalletToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }
}