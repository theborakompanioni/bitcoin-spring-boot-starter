package org.tbk.tor.spring.filter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tbk.tor.hs.HiddenServiceDefinition;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class OnionLocationHeaderFilterTest {

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    private File hostnameFile;

    @BeforeEach
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    void setUp() {
        this.request = new MockHttpServletRequest("GET", "/");
        this.request.setSecure(true);

        this.response = new MockHttpServletResponse();
        this.filterChain = new MockFilterChain();

        URL hostnameUrl = getClass().getClassLoader().getResource("org/tbk/tor/spring/filter/hostname");
        assertThat("sanity check", hostnameUrl, is(notNullValue()));

        this.hostnameFile = new File(hostnameUrl.getFile());

        assertThat("sanity check - hostname file must exist", this.hostnameFile.exists(), is(true));
        assertThat("sanity check - hostname file is readable", this.hostnameFile.canRead(), is(true));
    }

    @Test
    void noop() throws Exception {
        OnionLocationHeaderFilter sut = OnionLocationHeaderFilter.noop();
        sut.setAllowOnLocalhostWithHttp(true);

        sut.doFilter(request, response, filterChain);

        assertThat(response.getHeaderNames(), not(hasItem("Onion-Location")));
    }

    @Test
    void create() throws Exception {
        HiddenServiceDefinition build = HiddenServiceDefinition.builder()
                .directory(hostnameFile.getParentFile())
                .virtualPort(80)
                .port(80)
                .host(InetAddress.getLoopbackAddress().getHostName())
                .build();

        OnionLocationHeaderFilter sut = OnionLocationHeaderFilter.create(build);
        sut.setAllowOnLocalhostWithHttp(true);

        sut.doFilter(request, response, filterChain);

        assertThat(response.getHeaderNames(), hasItem("Onion-Location"));
        assertThat(response.getHeaderValue("Onion-Location"), is("http://test.onion/"));
    }
}