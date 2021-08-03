package org.tbk.lnurl.simple;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;

class SimpleLnurlTest {

    @Test
    void decode() {
        // example lnurl string taken from https://github.com/fiatjaf/lnurl-rfc
        String bech32lnurl = "LNURL1DP68GURN8GHJ7UM9WFMXJCM99E3K7MF0V9CXJ0M385EKVCENXC6R2C35XVUKXEFCV5MKVV34X5EKZD3EV56NYD3HXQURZEPEXEJXXEPNXSCRVWFNV9NXZCN9XQ6XYEFHVGCXXCMYXYMNSERXFQ5FNS";
        String expected = "https://service.com/api?q=3fc3645b439ce8e7f2553a69e5267081d96dcd340693afabe04be7b0ccd178df";

        SimpleLnurl lnurlParsed = SimpleLnurl.fromBech32(bech32lnurl);

        assertThat(lnurlParsed.toLnurlString(), equalToIgnoringCase(bech32lnurl));
        assertThat(lnurlParsed.toUri().toString(), is(expected));
    }

    @Test
    void encode() {
        String url = "https://service.com/api?q=3fc3645b439ce8e7f2553a69e5267081d96dcd340693afabe04be7b0ccd178df";
        String expectedBech32lnurl = "LNURL1DP68GURN8GHJ7UM9WFMXJCM99E3K7MF0V9CXJ0M385EKVCENXC6R2C35XVUKXEFCV5MKVV34X5EKZD3EV56NYD3HXQURZEPEXEJXXEPNXSCRVWFNV9NXZCN9XQ6XYEFHVGCXXCMYXYMNSERXFQ5FNS";

        URI uri = URI.create(url);
        SimpleLnurl lnurl = SimpleLnurl.fromUri(uri);

        assertThat(lnurl.toLnurlString().toUpperCase(), is(expectedBech32lnurl));
    }
}