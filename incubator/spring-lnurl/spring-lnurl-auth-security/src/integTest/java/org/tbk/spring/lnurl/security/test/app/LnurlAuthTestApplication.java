package org.tbk.spring.lnurl.security.test.app;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class LnurlAuthTestApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(LnurlAuthTestApplication.class)
                .web(WebApplicationType.SERVLET)
                .profiles("test")
                .run(args);
    }
}