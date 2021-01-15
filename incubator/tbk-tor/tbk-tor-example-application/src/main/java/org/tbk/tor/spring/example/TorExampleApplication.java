package org.tbk.tor.spring.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.context.WebServerPortFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

import java.nio.charset.StandardCharsets;

@Slf4j
@SpringBootApplication
public class TorExampleApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(TorExampleApplication.class)
                .listeners(applicationPidFileWriter())
                .web(WebApplicationType.SERVLET)
                .profiles("development", "local")
                .run(args);
    }

    public static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }

    public static ApplicationListener<?> webServerPortFileWriter() {
        return new WebServerPortFileWriter("application.port");
    }

    @Autowired
    @Qualifier("torHttpClient")
    private HttpClient torHttpClient;

    @Bean
    public ApplicationRunner mainRunner() {
        String successPhrase = "Congratulations. This browser is configured to use Tor.";
        String errorPhraseIgnoreCase = "not using Tor";

        return args -> {
            HttpGet req = new HttpGet("https://check.torproject.org/");

            HttpResponse rsp = torHttpClient.execute(req);

            String body = EntityUtils.toString(rsp.getEntity(), StandardCharsets.UTF_8);

            boolean containsErrorPhrase = body.toLowerCase().contains(errorPhraseIgnoreCase.toLowerCase());
            boolean containsSuccessPhrase = body.contains(successPhrase);

            boolean torEnabled = containsSuccessPhrase && !containsErrorPhrase;

            log.info("=================================================");
            if (torEnabled) {
                log.info("Tor is enabled.");
            } else {
                log.warn("Tor is NOT enabled.");
            }
            log.info("=================================================");
        };
    }
}
