package org.tbk.spring.lnurl.security;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.tbk.lnurl.auth.K1;
import org.tbk.lnurl.auth.K1Manager;
import org.tbk.lnurl.auth.LinkingKey;
import org.tbk.lnurl.auth.LnurlAuthPairingService;
import org.tbk.lnurl.test.SimpleLnurlWallet;
import org.tbk.spring.lnurl.security.session.LnurlAuthSessionToken;
import org.tbk.spring.lnurl.security.test.app.LnurlAuthTestApplication;

import java.net.URI;
import java.security.SecureRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = LnurlAuthTestApplication.class
)
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
@RecordApplicationEvents
class LnurlSessionAuthenticationFilterTest {

    @Autowired
    private K1Manager k1Manager;

    @Autowired
    private LnurlAuthPairingService pairingService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationEvents applicationEvents;

    private static SimpleLnurlWallet testWallet;

    @BeforeAll
    static void setUpAll() {
        byte[] seed = new SecureRandom().generateSeed(256);
        testWallet = SimpleLnurlWallet.fromSeed(seed);
    }

    @BeforeEach
    void setUp() {
        applicationEvents.clear();
    }

    @Test
    void sessionLoginMissingParamsError() throws Exception {
        mockMvc.perform(get(LnurlAuthConfigurer.defaultSessionLoginUrl()))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().bytes(new byte[0]));

        // generates no `AuthorizationFailureEvent` as success/failure events are published by an `AuthenticationManager`
        // which will not even be invoked for invalid requests in the first place.
        assertThat("no AuthorizationFailureEvent received", applicationEvents
                .stream(AbstractAuthenticationFailureEvent.class)
                .count(), is(0L));

        assertThat("no AuthenticationSuccessEvent received", applicationEvents
                .stream(AuthenticationSuccessEvent.class)
                .count(), is(0L));
    }

    @Test
    void sessionLoginSuccessBrowser() throws Exception {
        K1 k1 = k1Manager.create();

        // simulate linking with wallet
        LinkingKey linkingKey = testWallet.deriveLinkingPublicKey(URI.create("https://localhost"));
        pairingService.pairK1WithLinkingKey(k1, linkingKey);

        mockMvc.perform(get(LnurlAuthConfigurer.defaultSessionLoginUrl())
                .sessionAttr(LnurlAuthConfigurer.defaultSessionK1Key(), k1.toHex()))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"))
                .andExpect(content().bytes(new byte[0]));

        // assert that an `AuthenticationSuccessEvent` event has been received
        AuthenticationSuccessEvent authenticationSuccessEvent = applicationEvents
                .stream(AuthenticationSuccessEvent.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No AuthenticationSuccessEvent received"));

        assertThat(authenticationSuccessEvent.getAuthentication(), instanceOf(LnurlAuthSessionToken.class));

        LnurlAuthSessionToken sessionToken = (LnurlAuthSessionToken) authenticationSuccessEvent.getAuthentication();
        assertThat(sessionToken.getK1(), is(k1));
        assertThat(sessionToken.getLinkingKey(), is(linkingKey));
    }

    @Test
    void sessionLoginSuccessXhr() throws Exception {
        K1 k1 = k1Manager.create();

        // simulate linking with wallet
        LinkingKey linkingKey = testWallet.deriveLinkingPublicKey(URI.create("https://localhost"));
        pairingService.pairK1WithLinkingKey(k1, linkingKey);

        mockMvc.perform(get(LnurlAuthConfigurer.defaultSessionLoginUrl())
                .sessionAttr(LnurlAuthConfigurer.defaultSessionK1Key(), k1.toHex())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.headers").exists())
                .andExpect(jsonPath("$.headers.location").value("/"));

        // assert that an `AuthenticationSuccessEvent` event has been received
        AuthenticationSuccessEvent authenticationSuccessEvent = applicationEvents
                .stream(AuthenticationSuccessEvent.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No AuthenticationSuccessEvent received"));

        assertThat(authenticationSuccessEvent.getAuthentication(), instanceOf(LnurlAuthSessionToken.class));

        LnurlAuthSessionToken sessionToken = (LnurlAuthSessionToken) authenticationSuccessEvent.getAuthentication();
        assertThat(sessionToken.getK1(), is(k1));
        assertThat(sessionToken.getLinkingKey(), is(linkingKey));
    }

    @Test
    void sessionLoginNoPairingError() throws Exception {
        K1 k1 = k1Manager.create();

        mockMvc.perform(get(LnurlAuthConfigurer.defaultSessionLoginUrl())
                .sessionAttr(LnurlAuthConfigurer.defaultSessionK1Key(), k1.toHex()))
                .andExpect(status().isUnauthorized());

        // EXPECTED: verifying current behaviour: generates no `AuthorizationFailureEvent` as success/failure events as an
        // LnurlAuthenticationException is thrown, and the `DefaultAuthenticationEventPublisher` has no mapping
        // configured. Good for now, because a Session Migration Attempt fails often early on cause pairing with the
        // wallet depends on how long the users needs to scan the qr code, etc.

        // ACTUAL: Spring will currently trigger an unwanted `AuthenticationFailureProviderNotFoundEvent`
        // TODO: wait till https://github.com/spring-projects/spring-security/issues/10206 gets resolved.
        /*assertThat("no AuthorizationFailureEvent received", applicationEvents
                .stream(AbstractAuthenticationFailureEvent.class)
                .count(), is(0L));*/
    }
}