package org.tbk.spring.lnurl.security;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.tbk.lnurl.test.SimpleLnurlWallet;
import org.tbk.spring.lnurl.security.test.app2.LnurlAuthTestApplicationWithDeprecatedConfig;

import java.security.SecureRandom;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = LnurlAuthTestApplicationWithDeprecatedConfig.class
)
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
class LnurlAuthFlow2Test {
    private static final SecureRandom random = new SecureRandom();

    @Autowired
    private MockMvc mockMvc;

    private static SimpleLnurlWallet testWallet;

    @BeforeAll
    static void setUpAll() {
        byte[] seed = random.generateSeed(256);
        testWallet = SimpleLnurlWallet.fromSeed(seed);
    }

    @Test
    void lnurlAuthLoginSuccess() throws Exception {
        LnurlAuthFlowTestBase.testLnurlAuthLoginSuccess(mockMvc, testWallet);
    }
}