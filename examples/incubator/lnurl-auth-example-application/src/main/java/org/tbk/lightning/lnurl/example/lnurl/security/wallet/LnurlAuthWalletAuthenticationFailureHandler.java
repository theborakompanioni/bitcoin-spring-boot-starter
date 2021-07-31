package org.tbk.lightning.lnurl.example.lnurl.security.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Slf4j
class LnurlAuthWalletAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @SuppressFBWarnings("XSS_SERVLET") // false positive - a hardcoded value is written
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {
        log.debug("Received invalid lnurl-auth request: {}", e.getMessage());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        Map<String, String> errorBody = ImmutableMap.<String, String>builder()
                .put("status", "ERROR")
                .put("reason", "Request could not be authenticated.")
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}
