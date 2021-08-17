package org.tbk.spring.lnurl.security.wallet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
final class LnurlAuthWalletAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) {
        if (log.isDebugEnabled()) {
            log.debug("Received invalid lnurl-auth request: {}", e.getMessage());
        }
    }
}
