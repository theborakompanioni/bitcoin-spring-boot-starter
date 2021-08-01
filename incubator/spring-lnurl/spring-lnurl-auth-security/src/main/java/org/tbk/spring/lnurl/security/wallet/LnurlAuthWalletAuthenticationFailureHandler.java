package org.tbk.spring.lnurl.security.wallet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
final class LnurlAuthWalletAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final String body = "{\n"
            + "\"status\": \"ERROR\",\n"
            + "\"status\": \"Request could not be authenticated.\"\n"
            + "}";

    @Override
    @SuppressFBWarnings("XSS_SERVLET") // false positive - a hardcoded value is written
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {
        log.debug("Received invalid lnurl-auth request: {}", e.getMessage());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        response.getWriter().write(body);
    }
}
