package org.tbk.lnurl;

import java.net.URI;

import static java.util.Objects.requireNonNull;

public interface LnUrl {

    static boolean isSupported(URI uri) {
        requireNonNull(uri, "'uri' must not be null");

        if (!"https".equals(uri.getScheme())) {
            return "http".equals(uri.getScheme()) && uri.getHost().endsWith(".onion");
        }
        return true;
    }

    URI toUri();

    String toLnUrlString();
}
