package org.tbk.spring.lnurl.security.test.shared;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.tbk.lnurl.auth.LnurlAuthFactory;
import org.tbk.lnurl.auth.SimpleK1Manager;
import org.tbk.lnurl.auth.SimpleLnurlAuthFactory;
import org.tbk.spring.lnurl.security.LnurlAuthConfigurer;

import java.net.URI;

@Slf4j
@Configuration
@ComponentScan
public class LnurlAuthTestConfig {

    @Bean
    LnurlAuthFactory lnurlAuthFactory() {
        // any url well do - we just need the query string in tests
        URI loginUrl = URI.create("https://localhost" + LnurlAuthConfigurer.defaultWalletLoginUrl());

        return new SimpleLnurlAuthFactory(loginUrl, k1Manager());
    }

    @Bean
    TestPairingService pairingService() {
        return new TestPairingService(userDetailsService());
    }

    @Bean
    InMemoryUserDetailsManager userDetailsService() {
        return new InMemoryUserDetailsManager();
    }

    // make bean of K1Manager injectable in tests
    @Bean
    SimpleK1Manager k1Manager() {
        return new SimpleK1Manager();
    }

}
