package org.tbk.spring.lnurl.security.session;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;

import java.io.IOException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Slf4j
final class LnurlAuthSessionRedirectStrategy extends DefaultRedirectStrategy {

    private final RedirectStrategy delegate;

    LnurlAuthSessionRedirectStrategy(RedirectStrategy delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    @SuppressFBWarnings("XSS_SERVLET") // false positive
    public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        boolean clientPrefersJson = clientPrefersJson(request);
        if (!clientPrefersJson) {
            this.delegate.sendRedirect(request, response, url);
        } else {
            String targetUrl = calculateRedirectUrl(request.getContextPath(), url);
            String body = """
                    {
                      "status": "OK",
                      "headers": {
                        "location": "%s"
                      }
                    }""".formatted(targetUrl);

            // prevent redirection and write a json to the client indicating successful authentication
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(body);
        }
    }

    private boolean clientPrefersJson(HttpServletRequest request) {
        MediaType acceptMediaType = Optional.ofNullable(request.getHeader(HttpHeaders.ACCEPT))
                .map(MediaType::parseMediaTypes)
                .flatMap(it -> it.stream().findFirst())
                .orElse(MediaType.ALL);

        return MediaType.APPLICATION_JSON.equals(acceptMediaType);
    }
}
