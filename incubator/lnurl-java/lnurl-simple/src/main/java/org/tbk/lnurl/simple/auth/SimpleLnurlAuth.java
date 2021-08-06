package org.tbk.lnurl.simple.auth;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.tbk.lnurl.Lnurl;
import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.auth.LnurlAuth;
import org.tbk.lnurl.simple.SimpleLnurl;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
@Value
public class SimpleLnurlAuth implements LnurlAuth {

    public static SimpleLnurlAuth create(URI url, K1 k1) {
        return create(url, k1, null);
    }

    // https://example.com?tag=login&k1=hex(32 bytes of random data)&action=login
    public static SimpleLnurlAuth create(URI url, K1 k1, Action action) {
        return createInternal(url, k1, action);
    }

    public static SimpleLnurlAuth parse(Lnurl lnurl) {
        return parse(lnurl.toUri());
    }

    public static SimpleLnurlAuth parse(String uri) {
        return parse(URI.create(uri));
    }

    public static SimpleLnurlAuth parse(URI uri) {
        return parseInternal(uri);
    }

    URI baseUrl;
    K1 k1;
    Action action;

    private SimpleLnurlAuth(URI baseUrl, K1 k1, Action action) {
        this.baseUrl = requireNonNull(baseUrl);
        this.k1 = requireNonNull(k1);
        this.action = action;
    }

    @Override
    public Lnurl toLnurl() {
        return SimpleLnurl.fromUri(toUri());
    }

    @Override
    public Optional<Action> getAction() {
        return Optional.ofNullable(action);
    }

    @SneakyThrows(URISyntaxException.class)
    private URI toUri() {
        URIBuilder uriBuilder = new URIBuilder(baseUrl)
                .setParameter(LNURL_AUTH_TAG_KEY, LNURL_AUTH_TAG_PARAM_VALUE)
                .setParameter(LNURL_AUTH_K1_KEY, k1.toHex());

        this.getAction().ifPresent(it -> uriBuilder.setParameter(LNURL_AUTH_ACTION_KEY, it.getValue()));

        return uriBuilder.build();
    }

    // https://example.com?tag=login&k1=hex(32 bytes of random data)&action=login
    private static SimpleLnurlAuth createInternal(URI url, K1 k1, Action action) {
        List<NameValuePair> queryParams = URLEncodedUtils.parse(url, StandardCharsets.UTF_8);

        Map<String, List<NameValuePair>> queryParamsMap = queryParams.stream()
                .collect(Collectors.groupingBy(NameValuePair::getName));

        List<NameValuePair> tagQueryParams = queryParamsMap.getOrDefault(LNURL_AUTH_TAG_KEY, Collections.emptyList());
        checkArgument(tagQueryParams.isEmpty(), "Url must not include '" + LNURL_AUTH_TAG_KEY + "' query parameter");

        List<NameValuePair> k1QueryParams = queryParamsMap.getOrDefault(LNURL_AUTH_K1_KEY, Collections.emptyList());
        checkArgument(k1QueryParams.isEmpty(), "Url must not include '" + LNURL_AUTH_K1_KEY + "' query parameter");

        List<NameValuePair> actionQueryParams = queryParamsMap.getOrDefault(LNURL_AUTH_ACTION_KEY, Collections.emptyList());
        checkArgument(actionQueryParams.isEmpty(), "Url must not include '" + LNURL_AUTH_ACTION_KEY + "' query parameter");

        return new SimpleLnurlAuth(url, k1, action);
    }

    private static SimpleLnurlAuth parseInternal(URI uri) {
        requireNonNull(uri, "'uri' must not be null");

        if (!Lnurl.isSupported(uri)) {
            throw new IllegalArgumentException("Unsupported url: Only 'https' or 'onion' urls allowed");
        }

        Map<String, List<NameValuePair>> queryParamsMap = parseQueryParamsMap(uri);

        List<NameValuePair> tagQueryParams = queryParamsMap.getOrDefault(LNURL_AUTH_TAG_KEY, Collections.emptyList());
        checkArgument(tagQueryParams.size() == 1, "Url must include exactly one '" + LNURL_AUTH_TAG_KEY + "' query parameter");
        String tag = tagQueryParams.stream().map(NameValuePair::getValue).findFirst().orElseThrow();
        checkArgument(LNURL_AUTH_TAG_PARAM_VALUE.equals(tag), "Invalid '" + LNURL_AUTH_TAG_KEY + "' query parameter: Must have value '" + LNURL_AUTH_TAG_PARAM_VALUE + "'");

        List<NameValuePair> k1QueryParams = queryParamsMap.getOrDefault(LNURL_AUTH_K1_KEY, Collections.emptyList());
        checkArgument(k1QueryParams.size() == 1, "Url must include exactly one '" + LNURL_AUTH_K1_KEY + "' query parameter");
        String k1 = k1QueryParams.stream().map(NameValuePair::getValue).findFirst().orElseThrow();

        List<NameValuePair> actionQueryParams = queryParamsMap.getOrDefault(LNURL_AUTH_ACTION_KEY, Collections.emptyList());
        checkArgument(actionQueryParams.size() <= 1, "Url must not include more than one '" + LNURL_AUTH_ACTION_KEY + "' query parameter");
        Optional<Action> action = actionQueryParams.stream()
                .map(NameValuePair::getValue)
                .map(Action::parse)
                .findFirst();

        return new SimpleLnurlAuth(uri, SimpleK1.fromHex(k1), action.orElse(null));
    }

    private static Map<String, List<NameValuePair>> parseQueryParamsMap(URI uri) {
        List<NameValuePair> queryParams = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);

        return queryParams.stream()
                .collect(Collectors.groupingBy(NameValuePair::getName));
    }
}
