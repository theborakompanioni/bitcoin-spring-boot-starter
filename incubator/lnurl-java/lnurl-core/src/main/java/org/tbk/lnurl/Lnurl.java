package org.tbk.lnurl;

import java.net.URI;

import static java.util.Objects.requireNonNull;

public interface Lnurl {

    static boolean isSupported(URI uri) {
        requireNonNull(uri, "'uri' must not be null");

        if (!"https".equals(uri.getScheme())) {
            boolean isLocal = "localhost".equals(uri.getHost());
            boolean isTor = "http".equals(uri.getScheme()) && uri.getHost().endsWith(".onion");
            return isLocal || isTor;
        }
        return true;
    }

    URI toUri();

    String toLnurlString();
}
