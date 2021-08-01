package org.tbk.spring.lnurl.security.wallet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
final class LnurlAuthWalletAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String body = "{\n"
            + "\"status\": \"OK\"\n"
            + "}";

    @Override
    @SuppressFBWarnings("XSS_SERVLET") // false positive - a hardcoded value is written
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.debug("Received successful lnurl-auth request of user '{}'", authentication.getPrincipal());

        response.setStatus(HttpStatus.OK.value());

        response.getWriter().write(body);
    }
}
