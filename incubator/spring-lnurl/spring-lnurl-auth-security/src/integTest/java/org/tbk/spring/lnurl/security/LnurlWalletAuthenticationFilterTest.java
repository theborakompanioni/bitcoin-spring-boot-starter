package org.tbk.spring.lnurl.security;

import fr.acinq.secp256k1.Hex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.tbk.lnurl.auth.*;
import org.tbk.lnurl.simple.auth.SimpleK1;
import org.tbk.lnurl.simple.auth.SimpleLnurlAuth;
import org.tbk.lnurl.test.SimpleLnurlWallet;
import org.tbk.spring.lnurl.security.test.app1.LnurlAuthTestApplication;
import org.tbk.spring.lnurl.security.wallet.LnurlAuthWalletActionEvent;
import org.tbk.spring.lnurl.security.wallet.LnurlAuthWalletToken;

import java.net.URI;
import java.security.SecureRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = LnurlAuthTestApplication.class
)
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
@RecordApplicationEvents
class LnurlWalletAuthenticationFilterTest {
    private static final SecureRandom random = new SecureRandom();

    @Autowired
    private K1Manager k1Manager;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationEvents applicationEvents;

    private static SimpleLnurlWallet testWallet;

    @BeforeAll
    static void setUpAll() {
        byte[] seed = random.generateSeed(256);
        testWallet = SimpleLnurlWallet.fromSeed(seed);
    }

    @BeforeEach
    void setUp() {
        applicationEvents.clear();
    }

    @Test
    void walletLoginMissingParamsError() throws Exception {
        mockMvc.perform(get(LnurlAuthConfigurer.defaultWalletLoginUrl()))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.reason", is("Request could not be authenticated.")));

        // generates no `AuthorizationFailureEvent` as success/failure events are published by an `AuthenticationManager`
        // which will not even be invoked for invalid requests in the first place.
        assertThat("no AuthorizationFailureEvent received", applicationEvents
                .stream(AbstractAuthenticationFailureEvent.class)
                .count(), is(0L));

        assertThat("no AuthenticationSuccessEvent received", applicationEvents
                .stream(AuthenticationSuccessEvent.class)
                .count(), is(0L));

        assertThat("no LnurlAuthWalletActionEvent received", applicationEvents
                .stream(LnurlAuthWalletActionEvent.class)
                .count(), is(0L));
    }

    @Test
    void walletLoginSuccess() throws Exception {
        URI url = URI.create("https://localhost" + LnurlAuthConfigurer.defaultWalletLoginUrl());
        LnurlAuth lnurlAuth = SimpleLnurlAuth.create(url, k1Manager.create());

        SignedLnurlAuth signedLnurlAuth = testWallet.authorize(lnurlAuth);

        mockMvc.perform(get(signedLnurlAuth.toLnurl().toUri()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")));

        // assert that an `AuthenticationSuccessEvent` event has been received
        AuthenticationSuccessEvent authenticationSuccessEvent = applicationEvents
                .stream(AuthenticationSuccessEvent.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No AuthenticationSuccessEvent received"));

        assertThat(authenticationSuccessEvent.getAuthentication(), instanceOf(LnurlAuthWalletToken.class));

        LnurlAuthWalletToken walletToken = (LnurlAuthWalletToken) authenticationSuccessEvent.getAuthentication();
        assertThat(walletToken.getK1(), is(signedLnurlAuth.getK1()));
        assertThat(walletToken.getSignature(), is(signedLnurlAuth.getSignature()));
        assertThat(walletToken.getLinkingKey(), is(signedLnurlAuth.getLinkingKey()));

        // assert that an `LnurlAuthWalletActionEvent` event has been received
        LnurlAuthWalletActionEvent lnurlAuthWalletActionEvent = applicationEvents
                .stream(LnurlAuthWalletActionEvent.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No LnurlAuthWalletActionEvent received"));
        assertThat(lnurlAuthWalletActionEvent.getAuthentication(), is(walletToken));
        assertThat(lnurlAuthWalletActionEvent.getAction(), is(signedLnurlAuth.getAction()));
    }

    @Test
    void walletLoginInvalidK1() throws Exception {
        URI loginUri = URI.create("https://localhost" + LnurlAuthConfigurer.defaultWalletLoginUrl());
        LnurlAuth lnurlAuth = SimpleLnurlAuth.create(loginUri, k1Manager.create());

        SignedLnurlAuth signedLnurlAuth = testWallet.authorize(lnurlAuth);

        mockMvc.perform(get(loginUri)
                .queryParam(LnurlAuth.LNURL_AUTH_K1_KEY, "00".repeat(32))
                .queryParam(SignedLnurlAuth.LNURL_AUTH_SIG_KEY, signedLnurlAuth.getSignature().toHex())
                .queryParam(SignedLnurlAuth.LNURL_AUTH_KEY_KEY, signedLnurlAuth.getLinkingKey().toHex()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.reason", is("Request could not be authenticated.")));

        // verify `AuthenticationFailureBadCredentialsEvent` has been received
        AuthenticationFailureBadCredentialsEvent badCredentialsEvent = applicationEvents
                .stream(AuthenticationFailureBadCredentialsEvent.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No AuthenticationFailureBadCredentialsEvent received"));

        assertThat(badCredentialsEvent.getException().getMessage(), is("k1 value has either expired or was not generated by this service."));
        assertThat(badCredentialsEvent.getAuthentication(), instanceOf(LnurlAuthWalletToken.class));

        LnurlAuthWalletToken walletToken = (LnurlAuthWalletToken) badCredentialsEvent.getAuthentication();
        assertThat(walletToken.getK1().toHex(), is("00".repeat(32)));
        assertThat(walletToken.getSignature(), is(signedLnurlAuth.getSignature()));
        assertThat(walletToken.getLinkingKey(), is(signedLnurlAuth.getLinkingKey()));
    }

    @Test
    void walletLoginMismatchingSig() throws Exception {
        URI loginUri = URI.create("https://localhost" + LnurlAuthConfigurer.defaultWalletLoginUrl());

        K1 realK1 = k1Manager.create(); // known to the service - we use it to make the request
        K1 fakeK1 = SimpleK1.fromHex("00".repeat(32)); // unknown to service - we use it to sign the request
        LnurlAuth lnurlAuth = SimpleLnurlAuth.create(loginUri, fakeK1);

        SignedLnurlAuth signedLnurlAuth = testWallet.authorize(lnurlAuth);

        mockMvc.perform(get(loginUri)
                .queryParam(LnurlAuth.LNURL_AUTH_K1_KEY, realK1.toHex())
                .queryParam(SignedLnurlAuth.LNURL_AUTH_SIG_KEY, signedLnurlAuth.getSignature().toHex())
                .queryParam(SignedLnurlAuth.LNURL_AUTH_KEY_KEY, signedLnurlAuth.getLinkingKey().toHex()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.reason", is("Request could not be authenticated.")));

        // verify `AuthenticationFailureBadCredentialsEvent` has been received
        AuthenticationFailureBadCredentialsEvent badCredentialsEvent = applicationEvents
                .stream(AuthenticationFailureBadCredentialsEvent.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No AuthenticationFailureBadCredentialsEvent received"));

        assertThat(badCredentialsEvent.getException().getMessage(), is("k1 and signature could not be verified."));
        assertThat(badCredentialsEvent.getAuthentication(), instanceOf(LnurlAuthWalletToken.class));

        LnurlAuthWalletToken walletToken = (LnurlAuthWalletToken) badCredentialsEvent.getAuthentication();
        assertThat(walletToken.getK1(), is(realK1));
        assertThat(walletToken.getSignature(), is(signedLnurlAuth.getSignature()));
        assertThat(walletToken.getLinkingKey(), is(signedLnurlAuth.getLinkingKey()));
    }

    @Test
    void walletLoginInvalidKey() throws Exception {
        URI loginUri = URI.create("https://localhost" + LnurlAuthConfigurer.defaultWalletLoginUrl());

        LnurlAuth lnurlAuth = SimpleLnurlAuth.create(loginUri, k1Manager.create());

        SignedLnurlAuth signedLnurlAuth = testWallet.authorize(lnurlAuth);

        String invalidKeyHex = signedLnurlAuth.getLinkingKey().toHex().replaceAll("[0-9a-fA-F]", "0");

        mockMvc.perform(get(loginUri)
                .queryParam(LnurlAuth.LNURL_AUTH_K1_KEY, signedLnurlAuth.getK1().toHex())
                .queryParam(SignedLnurlAuth.LNURL_AUTH_SIG_KEY, signedLnurlAuth.getSignature().toHex())
                .queryParam(SignedLnurlAuth.LNURL_AUTH_KEY_KEY, invalidKeyHex))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.reason", is("Request could not be authenticated.")));

        // generates no `AuthorizationFailureEvent` as success/failure events are published by an `AuthenticationManager`
        // which will not even be invoked for invalid requests in the first place.
        // in this case the linking key is not a valid linking key.
        assertThat("no AuthorizationFailureEvent received", applicationEvents
                .stream(AbstractAuthenticationFailureEvent.class)
                .count(), is(0L));

        assertThat("no AuthenticationSuccessEvent received", applicationEvents
                .stream(AuthenticationSuccessEvent.class)
                .count(), is(0L));

        assertThat("no LnurlAuthWalletActionEvent received", applicationEvents
                .stream(LnurlAuthWalletActionEvent.class)
                .count(), is(0L));
    }

    @Test
    void walletLoginMismatchingKey() throws Exception {
        URI loginUri = URI.create("https://localhost" + LnurlAuthConfigurer.defaultWalletLoginUrl());

        LnurlAuth lnurlAuth = SimpleLnurlAuth.create(loginUri, k1Manager.create());

        SignedLnurlAuth signedLnurlAuth = testWallet.authorize(lnurlAuth);

        LinkingKey mismatchingKey = SimpleLnurlWallet.fromSeed(Hex.decode("00".repeat(256))).deriveLinkingPublicKey(loginUri);

        mockMvc.perform(get(loginUri)
                .queryParam(LnurlAuth.LNURL_AUTH_K1_KEY, signedLnurlAuth.getK1().toHex())
                .queryParam(SignedLnurlAuth.LNURL_AUTH_SIG_KEY, signedLnurlAuth.getSignature().toHex())
                .queryParam(SignedLnurlAuth.LNURL_AUTH_KEY_KEY, mismatchingKey.toHex()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.reason", is("Request could not be authenticated.")));

        // verify `AuthenticationFailureBadCredentialsEvent` has been sent
        AuthenticationFailureBadCredentialsEvent badCredentialsEvent = applicationEvents
                .stream(AuthenticationFailureBadCredentialsEvent.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No AuthenticationFailureBadCredentialsEvent received"));

        assertThat(badCredentialsEvent.getException().getMessage(), is("k1 and signature could not be verified."));
        assertThat(badCredentialsEvent.getAuthentication(), instanceOf(LnurlAuthWalletToken.class));

        LnurlAuthWalletToken walletToken = (LnurlAuthWalletToken) badCredentialsEvent.getAuthentication();
        assertThat(walletToken.getK1(), is(signedLnurlAuth.getK1()));
        assertThat(walletToken.getSignature(), is(signedLnurlAuth.getSignature()));
        assertThat(walletToken.getLinkingKey(), is(mismatchingKey));
    }
}