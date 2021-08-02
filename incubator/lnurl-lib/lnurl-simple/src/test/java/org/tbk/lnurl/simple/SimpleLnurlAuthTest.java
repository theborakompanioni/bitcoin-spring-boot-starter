package org.tbk.lnurl.simple;

import org.junit.jupiter.api.Test;
import org.tbk.lnurl.Lnurl;
import org.tbk.lnurl.simple.auth.SimpleLnurlAuth;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimpleLnurlAuthTest {

    @Test
    void create() {
        URI url = URI.create("http://example.onion");

        SimpleLnurlAuth lnUrlAuth = SimpleLnurlAuth.create(url);
        Lnurl lnurlCreated = lnUrlAuth.toLnUrl();

        assertThat(lnurlCreated.toUri().getScheme(), is(url.getScheme()));
        assertThat(lnurlCreated.toUri().getHost(), is(url.getHost()));
        assertThat(lnurlCreated.toLnUrlString(), is(notNullValue()));
    }

    @Test
    void fromUri() {
        URI uri = URI.create("http://example.onion?tag=login&action=login&k1=cb5a02549b96609c99818cf587a9954da337222591df284d36db9114bd430cb3");
        String expectedLnurl = "lnurl1dp68gup69uhk27rpd4cxcefwdahxjmmw8a6xzeead3hkw6twye4nz0trvg6kzvpjx56rjc3excmrqwtr8yunsvfcvdnr2wphvyunjdf5v3snxvehxgerydfex9jxvv3cx3jrxdnyvgunzvf5vfjrgvesvd3rxfnpvd6xjmmw84kx7emfdcwfl4xp";

        SimpleLnurlAuth lnUrlAuth = SimpleLnurlAuth.from(uri);
        Lnurl lnurlCreated = lnUrlAuth.toLnUrl();

        assertThat(lnurlCreated.toLnUrlString(), is(expectedLnurl));
    }

    @Test
    void itShouldThrowWhenParsingInvalidUrls() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> SimpleLnurlAuth.parse("http://example.com"));
        assertThat(e.getMessage(), is("Unsupported url: Only 'https' or 'onion' urls allowed"));

        IllegalArgumentException e1 = assertThrows(IllegalArgumentException.class, () -> SimpleLnurlAuth.parse("sftp://example.com"));
        assertThat(e1.getMessage(), is("Unsupported url: Only 'https' or 'onion' urls allowed"));

        IllegalArgumentException e2 = assertThrows(IllegalArgumentException.class, () -> SimpleLnurlAuth.parse("https://example.onion"));
        assertThat(e2.getMessage(), is("Url must include exactly one 'tag' query parameter"));

        IllegalArgumentException e3 = assertThrows(IllegalArgumentException.class, () -> SimpleLnurlAuth.parse("https://example.onion?tag=login"));
        assertThat(e3.getMessage(), is("Url must include exactly one 'k1' query parameter"));
    }

    @Test
    void itShouldNotThrowWhenParsingValidUrls() {
        String query = "?tag=login&action=login&k1=0000000000000000000000000000000000000000000000000000000000000000";
        assertDoesNotThrow(() -> SimpleLnurlAuth.parse("https://example.com" + query));
        assertDoesNotThrow(() -> SimpleLnurlAuth.parse("http://example.onion" + query));
        assertDoesNotThrow(() -> SimpleLnurlAuth.parse("https://example.onion" + query));
    }
}