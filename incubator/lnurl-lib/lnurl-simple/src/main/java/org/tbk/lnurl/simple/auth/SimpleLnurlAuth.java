package org.tbk.lnurl.simple.auth;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.Lnurl;
import org.tbk.lnurl.auth.LnurlAuth;
import org.tbk.lnurl.simple.SimpleLnurl;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
@Value
public class SimpleLnurlAuth implements LnurlAuth {
    public static final String TAG_PARAM_VALUE = "login";

    URI baseUrl;
    K1 k1;
    Action action;

    private SimpleLnurlAuth(URI baseUrl, K1 k1) {
        this(baseUrl, k1, null);
    }

    private SimpleLnurlAuth(URI baseUrl, K1 k1, Action action) {
        this.baseUrl = requireNonNull(baseUrl);
        this.k1 = requireNonNull(k1);
        this.action = action;
    }

    // https://example.com?tag=login&k1=hex(32 bytes of random data)&action=login
    public static SimpleLnurlAuth create(URI url) {
        return new SimpleLnurlAuth(url, SimpleK1.random());
    }

    public static SimpleLnurlAuth create(URI url, K1 k1) {
        return new SimpleLnurlAuth(url, k1);
    }

    public static SimpleLnurlAuth from(Lnurl lnurl) {
        return from(lnurl.toUri());
    }

    public static SimpleLnurlAuth parse(String uri) {
        return from(URI.create(uri));
    }

    public static SimpleLnurlAuth from(URI uri) {
        requireNonNull(uri, "'uri' must not be null");

        if (!Lnurl.isSupported(uri)) {
            throw new IllegalArgumentException("Unsupported url: Only 'https' or 'onion' urls allowed");
        }

        List<NameValuePair> queryParams = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);

        Map<String, List<NameValuePair>> queryParamsMap = queryParams.stream()
                .collect(Collectors.groupingBy(NameValuePair::getName));

        List<NameValuePair> tagQueryParams = queryParamsMap.getOrDefault("tag", Collections.emptyList());
        checkArgument(tagQueryParams.size() == 1, "Url must include exactly one 'tag' query parameter");
        String tag = tagQueryParams.stream().map(NameValuePair::getValue).findFirst().orElseThrow();
        checkArgument(TAG_PARAM_VALUE.equals(tag), "Invalid 'tag' query parameter: Must have value '" + TAG_PARAM_VALUE + "'");

        List<NameValuePair> k1QueryParams = queryParamsMap.getOrDefault("k1", Collections.emptyList());
        checkArgument(k1QueryParams.size() == 1, "Url must include exactly one 'k1' query parameter");
        String k1 = k1QueryParams.stream().map(NameValuePair::getValue).findFirst().orElseThrow();

        List<NameValuePair> actionQueryParams = queryParamsMap.getOrDefault("action", Collections.emptyList());
        checkArgument(actionQueryParams.size() <= 1, "Url must not include more than one 'action' query parameter");
        Optional<Action> action = actionQueryParams.stream()
                .map(NameValuePair::getValue)
                .map(Action::parse)
                .findFirst();

        return new SimpleLnurlAuth(uri, SimpleK1.fromHex(k1), action.orElse(null));
    }

    @Override
    @SneakyThrows(URISyntaxException.class)
    public URI toUri() {
        URIBuilder uriBuilder = new URIBuilder(baseUrl)
                .setParameter("tag", TAG_PARAM_VALUE)
                .setParameter("k1", k1.toHex());

        this.getAction().ifPresent(it -> uriBuilder.setParameter("action", it.getValue()));

        return uriBuilder.build();
    }

    @Override
    public Lnurl toLnurl() {
        return SimpleLnurl.fromUri(toUri());
    }

    @Override
    public Optional<Action> getAction() {
        return Optional.ofNullable(action);
    }

}
