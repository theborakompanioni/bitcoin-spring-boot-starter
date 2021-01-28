package org.tbk.tor.spring.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.tbk.tor.hs.HiddenServiceDefinition;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Slf4j
public class OnionLocationHeaderFilter extends OncePerRequestFilter {
    private static final String HEADER_NAME = "Onion-Location";

    public static OnionLocationHeaderFilter noop() {
        return new OnionLocationHeaderFilter();
    }

    public static OnionLocationHeaderFilter create(HiddenServiceDefinition hiddenService) {
        return new OnionLocationHeaderFilter(hiddenService);
    }

    private final HiddenServiceDefinition hiddenService;

    private final boolean enabled;
    private boolean allowOnLocalhostWithHttp = false;

    private OnionLocationHeaderFilter(HiddenServiceDefinition hiddenService) {
        this.hiddenService = requireNonNull(hiddenService);
        this.enabled = true;
    }

    private OnionLocationHeaderFilter() {
        this.hiddenService = null;
        this.enabled = false;
    }

    /**
     * For the header to be valid the following conditions need to be fulfilled:
     * - The Onion-Location value must be a valid URL with http: or https: protocol and a .onion hostname.
     * - The webpage defining the Onion-Location header must be served over HTTPS.
     * - The webpage defining the Onion-Location header must not be an onion site.
     * <p>
     * See specification on https://gitweb.torproject.org/tor-browser-spec.git/tree/proposals/100-onion-location-header.txt
     *
     * <p><strong>Note:</strong> This filter do not extract {@code "Forwarded"} and
     * {@code "X-Forwarded-*"} headers that specify the client-originated address.
     * Please, use {@link org.springframework.web.filter.ForwardedHeaderFilter},
     * or similar from the underlying server, to extract and use such headers,
     * or to discard them.
     *
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!enabled) {
            if (log.isTraceEnabled()) {
                log.trace("Skip adding '{}' header - filter is not enabled.", HEADER_NAME);
            }
        } else {
            addOnionHeaderIfPossible(request, response);
        }

        filterChain.doFilter(request, response);
    }

    private void addOnionHeaderIfPossible(HttpServletRequest request, HttpServletResponse response) {
        boolean onionLocationHeaderSupported = supportsAddingOnionLocationHeader(request, response);

        if (!onionLocationHeaderSupported) {
            if (log.isTraceEnabled()) {
                log.trace("Skip adding '{}' header - request is not supported.", HEADER_NAME);
            }
            return;
        }

        String virtualHost = hiddenService.getVirtualHost().orElseThrow();
        int virtualPort = hiddenService.getVirtualPort();

        String scheme = request.getScheme();
        boolean addPort = ("http".equals(scheme) && virtualPort != 80) ||
                ("https".equals(scheme) && virtualPort != 443);

        String portPart = addPort ? ":" + virtualPort : "";

        String onionUrl2 = ServletUriComponentsBuilder.fromRequestUri(request)
                .host(virtualHost)
                .port("" + (addPort ? hiddenService.getVirtualPort() : "-1"))
                .scheme("http")
                .build()
                .toUriString();

        String onionUrl = String.format("http://%s%s%s", hiddenService.getVirtualHost().orElse(null),
                portPart, request.getRequestURI());

        response.addHeader(HEADER_NAME, onionUrl);
    }

    private boolean supportsAddingOnionLocationHeader(HttpServletRequest request, HttpServletResponse response) {
        boolean hasOnionLocationHeader = !response.getHeaders(HEADER_NAME).isEmpty();
        boolean isVirtualHostnameAvailable = hiddenService.getVirtualHost().isPresent();
        boolean isAllowedOnLocalhostWithHttp = this.allowOnLocalhostWithHttp &&
                ("localhost".equals(request.getServerName()) ||
                        "127.0.0.1".equals(request.getServerName()));
        boolean isSecureRequest = request.isSecure() || isAllowedOnLocalhostWithHttp;
        boolean isSupportedMethod = HttpMethod.GET.matches(request.getMethod()) ||
                HttpMethod.HEAD.matches(request.getMethod()) ||
                HttpMethod.OPTIONS.matches(request.getMethod());

        boolean isServedByNonOnionSite = Optional.of(request.getServerName())
                .map(it -> !it.endsWith(".onion"))
                .orElseGet(() -> {
                    // assume served by onion site if we cannot determine. lets play it safe.
                    return false;
                });

        return !hasOnionLocationHeader &&
                isSupportedMethod &&
                isVirtualHostnameAvailable &&
                isSecureRequest &&
                isServedByNonOnionSite;
    }

    public boolean isAllowOnLocalhostWithHttp() {
        return allowOnLocalhostWithHttp;
    }

    public void setAllowOnLocalhostWithHttp(boolean allowOnLocalhostWithHttp) {
        this.allowOnLocalhostWithHttp = allowOnLocalhostWithHttp;
    }
}
