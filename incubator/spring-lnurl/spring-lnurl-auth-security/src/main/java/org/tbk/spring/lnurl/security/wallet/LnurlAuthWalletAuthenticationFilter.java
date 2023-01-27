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
import org.springframework.web.util.UriComponentsBuilder;
import org.tbk.lnurl.auth.*;
import org.tbk.lnurl.simple.auth.SimpleK1;
import org.tbk.lnurl.simple.auth.SimpleLinkingKey;
import org.tbk.lnurl.simple.auth.SimpleSignature;
import org.tbk.lnurl.simple.auth.SimpleSignedLnurlAuth;
import org.tbk.spring.lnurl.security.LnurlAuthenticationException;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

@Slf4j
public class LnurlAuthWalletAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private static final LnurlAuthWalletAuthenticationFailureHandler failureHandler = new LnurlAuthWalletAuthenticationFailureHandler();
    private static final LnurlAuthWalletAuthenticationSuccessHandler successHandler = new LnurlAuthWalletAuthenticationSuccessHandler();

    private static final String successBody = "{\n"
            + "  \"status\": \"OK\"\n"
            + "}";

    private static final String errorBody = "{\n"
            + "  \"status\": \"ERROR\",\n"
            + "  \"reason\": \"Request could not be authenticated.\"\n"
            + "}";

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
            log.debug("got lnurl-auth wallet authentication request for k1 '{}'", authRequest.getK1().toHex());
        }

        setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);

        if (authResult instanceof LnurlAuthWalletToken walletToken && this.eventPublisher != null) {

            URI requestUri = UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request)).build().toUri();
            SignedLnurlAuth signedLnurlAuth = SimpleSignedLnurlAuth.from(requestUri);

            this.eventPublisher.publishEvent(new LnurlAuthWalletActionEvent(this, walletToken, signedLnurlAuth));
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
            K1 k1 = obtainK1(request).orElseThrow(() -> new LnurlAuthenticationException("k1 is missing or invalid."));
            Signature sig = obtainSig(request).orElseThrow(() -> new LnurlAuthenticationException("Signature is missing or invalid."));
            LinkingKey key = obtainKey(request).orElseThrow(() -> new LnurlAuthenticationException("Key is missing or invalid."));

            return new LnurlAuthWalletToken(k1, sig, key);
        } catch (LnurlAuthenticationException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new LnurlAuthenticationException(e.getMessage(), e);
        } catch (Exception e) {
            throw new LnurlAuthenticationException("Cannot build wallet token from request", e);
        }
    }

    protected Optional<K1> obtainK1(HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(LnurlAuth.LNURL_AUTH_K1_KEY))
                .map(SimpleK1::fromHex);
    }

    protected Optional<Signature> obtainSig(HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(SignedLnurlAuth.LNURL_AUTH_SIG_KEY))
                .map(SimpleSignature::fromHex);
    }

    protected Optional<LinkingKey> obtainKey(HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(SignedLnurlAuth.LNURL_AUTH_KEY_KEY))
                .map(SimpleLinkingKey::fromHex);
    }

    protected void setDetails(HttpServletRequest request, LnurlAuthWalletToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }
}