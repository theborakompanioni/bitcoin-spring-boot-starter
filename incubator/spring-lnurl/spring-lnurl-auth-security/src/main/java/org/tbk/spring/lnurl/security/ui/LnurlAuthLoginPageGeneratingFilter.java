package org.tbk.spring.lnurl.security.ui;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;
import org.tbk.lnurl.auth.LnurlAuth;
import org.tbk.lnurl.auth.LnurlAuthFactory;
import org.tbk.spring.lnurl.security.ui.LoginScriptGenerator.ScriptConfig;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * For internal use with namespace configuration in the case where a user doesn't
 * configure a login page. The configuration code will insert this filter in the chain
 * instead.
 * <p>
 * Will only work if a redirect is used to the login page.
 * There is currently no way to customize the behaviour.
 * Depending on demand for customizations future implementations will be more configurable.
 */
@Slf4j
public class LnurlAuthLoginPageGeneratingFilter extends GenericFilterBean {

    private final static String HTML_CONTENT_TYPE = new MediaType("text", "html", StandardCharsets.UTF_8).toString();
    private final static String STYLESHEET_CONTENT_TYPE = new MediaType("text", "css", StandardCharsets.UTF_8).toString();
    private final static String JAVASCRIPT_CONTENT_TYPE = new MediaType("application", "javascript", StandardCharsets.UTF_8).toString();

    @Setter
    @Getter
    private boolean enabled = true;

    private final String k1AttributeName;

    // the url to the login page html should be delivered
    private final String loginPageUrl;

    // the url for browser session migration after wallet signed in
    private final String sessionAuthenticationUrl;

    private final LnurlAuthFactory lnurlAuthFactory;

    private final String logoutSuccessUrl;

    private final String failureUrl;

    private final String loginScriptUrl;

    private final String loginStylesheetUrl;

    private final LoginPageGenerator loginPageGenerator;

    public LnurlAuthLoginPageGeneratingFilter(LnurlAuthFactory lnurlAuthFactory,
                                              String defaultLoginPageUrl,
                                              String sessionAuthenticationUrl,
                                              String k1AttributeName) {
        Assert.hasText(k1AttributeName, "'k1AttributeName' must not be empty");
        Assert.hasText(defaultLoginPageUrl, "'defaultLoginPageUrl' must not be empty");
        Assert.hasText(sessionAuthenticationUrl, "'sessionAuthenticationUrl' must not be empty");

        this.lnurlAuthFactory = requireNonNull(lnurlAuthFactory);
        this.loginPageUrl = defaultLoginPageUrl;
        this.sessionAuthenticationUrl = sessionAuthenticationUrl;
        this.k1AttributeName = k1AttributeName;

        this.logoutSuccessUrl = this.loginPageUrl + "?logout";
        this.failureUrl = this.loginPageUrl + "?error";
        this.loginScriptUrl = this.loginPageUrl + "?script=default";
        this.loginStylesheetUrl = this.loginPageUrl + "?stylesheet=default";

        this.loginPageGenerator = new LoginPageGenerator(ScriptConfig.builder()
                .initialDelay(Duration.ofSeconds(3))
                .pollingInterval(Duration.ofSeconds(3))
                .maxAttempts(100)
                .build());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (this.isEnabled()) {
            boolean scriptRequest = isLoginScriptRequest(request);
            if (scriptRequest) {
                writeLoginScript(request, response);
                return;
            }

            boolean stylesheetRequest = isLoginStylesheetRequest(request);
            if (stylesheetRequest) {
                writeLoginStylesheet(request, response);
                return;
            }

            boolean loginPageRequest = isLoginUrlRequest(request)
                    || isLogoutSuccess(request)
                    || isErrorPage(request);
            if (loginPageRequest) {
                LnurlAuth lnurlAuth = lnurlAuthFactory.createLnUrlAuth();

                // we do not want already logged-in users to generate a new k1 value
                // the polling script will trigger errors and the user will be logged out
                // two solutions:
                //   1. redirect user to other page
                //   2. show a default "you are already logged in" message (without loading the script)
                boolean alreadyLoggedIn = request.getUserPrincipal() != null;
                if (!alreadyLoggedIn) {
                    // create session so the user is identified
                    HttpSession session = request.getSession(true);
                    session.setAttribute(k1AttributeName, lnurlAuth.getK1().toHex());
                }

                writeLoginPage(request, response, lnurlAuth);

                return;
            }
        }

        chain.doFilter(request, response);
    }

    @SuppressFBWarnings("XSS_SERVLET") // false positive
    private void writeLoginStylesheet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String script = this.loginPageGenerator.createStylesheet();

        response.setContentType(STYLESHEET_CONTENT_TYPE);
        response.setContentLength(script.getBytes(StandardCharsets.UTF_8).length);
        response.getWriter().write(script);
    }

    @SuppressFBWarnings("XSS_SERVLET") // false positive
    private void writeLoginScript(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // prevent logged-in users from invoking browser session migration - this would log out the user
        // as an AuthenticationException is thrown, which invalidates the user's authentication.
        // it is better not to load the script, regardless the configuration of the underlying application.
        // better: 1) redirect authenticated users before the login page is loaded or
        //         2) requesting the login page will trigger a logout -> user needs to log in again.
        String authenticationUrl = request.getContextPath() + this.sessionAuthenticationUrl;

        Optional<String> errorMessage = Optional.ofNullable(request.getSession(false))
                .map(it -> it.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION))
                .map(it -> (AuthenticationException) it)
                .map(Throwable::getMessage);

        String content = this.loginPageGenerator.createScript(authenticationUrl, request.getUserPrincipal(), errorMessage.orElse(null));

        response.setContentType(JAVASCRIPT_CONTENT_TYPE);
        response.setContentLength(content.getBytes(StandardCharsets.UTF_8).length);
        response.getWriter().write(content);
    }

    private void writeLoginPage(HttpServletRequest request, HttpServletResponse response, LnurlAuth lnurlAuth) throws IOException {
        LnurlQrcode lnurlQrcode = new LnurlQrcode(lnurlAuth.toLnurl(), 300);

        String stylesheetUrl = request.getContextPath() + this.loginStylesheetUrl;
        String scriptUrl = request.getContextPath() + this.loginScriptUrl;

        String content = this.loginPageGenerator.createLoginPage(stylesheetUrl, scriptUrl, lnurlQrcode);

        writeHtml(request, response, content);
    }

    /*private void writeAlreadyLoggedInPage(HttpServletRequest request, HttpServletResponse response, String errorMessage) throws IOException {
        writeLoginHtml(request, response, () -> {

            String stylesheetUrl = request.getContextPath() + this.loginStylesheetUrl;
            String scriptUrl = request.getContextPath() + this.loginScriptUrl;

            return this.loginPageGenerator.createErrorPage(stylesheetUrl, scriptUrl, errorMessage);
        });
    }*/

    /*
    private void writeLoginErrorPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String errorMessage = Optional.ofNullable(request.getSession(false))
                .map(session -> session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION))
                .map(ex -> (AuthenticationException) ex)
                .map(Throwable::getMessage)
                .orElse("Unknown Error");

        String stylesheetUrl = request.getContextPath() + this.loginStylesheetUrl;
        String scriptUrl = request.getContextPath() + this.loginScriptUrl;

        String content = this.loginPageGenerator.createErrorPage(stylesheetUrl, scriptUrl, errorMessage);

        writeHtml(request, response, content);
    }*/

    @SuppressFBWarnings("XSS_SERVLET") // false positive
    private void writeHtml(HttpServletRequest request, HttpServletResponse response, String content) throws IOException {
        response.setContentType(HTML_CONTENT_TYPE);
        response.setContentLength(content.getBytes(StandardCharsets.UTF_8).length);
        response.getWriter().write(content);
    }

    private boolean isLogoutSuccess(HttpServletRequest request) {
        return matches(request, this.logoutSuccessUrl);
    }

    private boolean isLoginUrlRequest(HttpServletRequest request) {
        return matches(request, this.loginPageUrl);
    }

    private boolean isErrorPage(HttpServletRequest request) {
        return matches(request, this.failureUrl);
    }

    private boolean isLoginScriptRequest(HttpServletRequest request) {
        return matches(request, this.loginScriptUrl);
    }

    private boolean isLoginStylesheetRequest(HttpServletRequest request) {
        return matches(request, this.loginStylesheetUrl);
    }

    private boolean matches(HttpServletRequest request, String url) {
        if (url == null || !HttpMethod.GET.name().equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        int pathParamIndex = uri.indexOf(';');
        if (pathParamIndex > 0) {
            // strip everything after the first semicolon
            uri = uri.substring(0, pathParamIndex);
        }
        if (request.getQueryString() != null) {
            uri += "?" + request.getQueryString();
        }
        if ("".equals(request.getContextPath())) {
            return uri.equals(url);
        }
        return uri.equals(request.getContextPath() + url);
    }
}