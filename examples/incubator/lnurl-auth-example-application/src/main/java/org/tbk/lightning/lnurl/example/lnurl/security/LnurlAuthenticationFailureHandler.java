package org.tbk.lightning.lnurl.example.lnurl.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Slf4j
class LnurlAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {
        log.debug("Received invalid lnurl-auth request: {}", e.getMessage());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        Map<String, String> errorBody = ImmutableMap.<String, String>builder()
                .put("status", "ERROR")
                .put("reason", "Request could not be authenticated.")
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
        response.flushBuffer();
    }
}
