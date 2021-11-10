package org.tbk.lnurl.simple.auth;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.tbk.lnurl.Lnurl;
import org.tbk.lnurl.auth.*;
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
public class SimpleSignedLnurlAuth implements SignedLnurlAuth {

    LnurlAuth lnurlAuth;
    LinkingKey linkingKey;
    Signature signature;

    private SimpleSignedLnurlAuth(LnurlAuth lnurlAuth, LinkingKey linkingKey, Signature signature) {
        this.lnurlAuth = requireNonNull(lnurlAuth);
        this.linkingKey = requireNonNull(linkingKey);
        this.signature = requireNonNull(signature);
    }

    public static SimpleSignedLnurlAuth create(LnurlAuth lnurlAuth, LinkingKey linkingKey, Signature signature) {
        return new SimpleSignedLnurlAuth(lnurlAuth, linkingKey, signature);
    }

    public static SimpleSignedLnurlAuth from(Lnurl lnurl) {
        return from(lnurl.toUri());
    }

    public static SimpleSignedLnurlAuth parse(String uri) {
        return from(URI.create(uri));
    }

    public static SimpleSignedLnurlAuth from(URI uri) {
        requireNonNull(uri, "'uri' must not be null");

        SimpleLnurlAuth lnurlAuth = SimpleLnurlAuth.parse(uri);
        List<NameValuePair> queryParams = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);

        Map<String, List<NameValuePair>> queryParamsMap = queryParams.stream()
                .collect(Collectors.groupingBy(NameValuePair::getName));

        List<NameValuePair> keyQueryParams = queryParamsMap.getOrDefault(LNURL_AUTH_KEY_KEY, Collections.emptyList());
        checkArgument(keyQueryParams.size() == 1, "Url must include exactly one '" + LNURL_AUTH_KEY_KEY + "' query parameter");
        String key = keyQueryParams.stream().map(NameValuePair::getValue).findFirst().orElseThrow();

        List<NameValuePair> sigQueryParams = queryParamsMap.getOrDefault(LNURL_AUTH_SIG_KEY, Collections.emptyList());
        checkArgument(sigQueryParams.size() == 1, "Url must include exactly one '" + LNURL_AUTH_SIG_KEY + "' query parameter");
        String sig = sigQueryParams.stream().map(NameValuePair::getValue).findFirst().orElseThrow();

        return new SimpleSignedLnurlAuth(lnurlAuth, SimpleLinkingKey.fromHex(key), SimpleSignature.fromHex(sig));
    }

    @Override
    public Lnurl toLnurl() {
        return SimpleLnurl.fromUri(toUri());
    }

    @Override
    public K1 getK1() {
        return this.lnurlAuth.getK1();
    }

    @Override
    public Optional<Action> getAction() {
        return this.lnurlAuth.getAction();
    }

    @SneakyThrows(URISyntaxException.class)
    private URI toUri() {
        URIBuilder uriBuilder = new URIBuilder(this.lnurlAuth.toLnurl().toUri())
                .setParameter(LNURL_AUTH_SIG_KEY, this.signature.toHex())
                .setParameter(LNURL_AUTH_KEY_KEY, this.linkingKey.toHex());

        return uriBuilder.build();
    }
}
