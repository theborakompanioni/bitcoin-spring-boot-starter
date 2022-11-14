package org.tbk.spring.lnurl.security.test.app1;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;
import org.tbk.spring.lnurl.security.test.shared.LnurlAuthTestConfig;

@SpringBootApplication
@Import(LnurlAuthTestConfig.class)
public class LnurlAuthTestApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(LnurlAuthTestApplication.class)
                .web(WebApplicationType.SERVLET)
                .profiles("test")
                .run(args);
    }
}