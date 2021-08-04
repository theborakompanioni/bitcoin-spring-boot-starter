package org.tbk.spring.lnurl.security.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
final class LnurlAuthSessionAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    LnurlAuthSessionAuthenticationSuccessHandler() {
        this.setRedirectStrategy(new LnurlAuthSessionRedirectStrategy(this.getRedirectStrategy()));
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        if (log.isDebugEnabled()) {
            log.debug("Received successful lnurl-auth session request of user '{}'", authentication.getPrincipal());
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
