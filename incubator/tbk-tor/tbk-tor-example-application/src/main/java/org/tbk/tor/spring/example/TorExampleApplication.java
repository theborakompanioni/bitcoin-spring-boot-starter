package org.tbk.tor.spring.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.berndpruenster.netlayer.tor.Tor;
import org.berndpruenster.netlayer.tor.TorCtlException;
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
import org.springframework.context.annotation.Profile;
import org.tbk.tor.hs.HiddenServiceDefinition;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

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
    private Tor tor;

    @Autowired
    @Qualifier("torHttpClient")
    private HttpClient torHttpClient;

    @Autowired
    @Qualifier("applicationHiddenServiceDefinition")
    private HiddenServiceDefinition applicationHiddenServiceDefinition;

    @Bean
    @Profile("!test")
    public ApplicationRunner applicationHiddenServiceInfoRunner() {
        return args -> {
            Optional<String> httpUrl = applicationHiddenServiceDefinition.getVirtualHost()
                    .map(val -> "http://" + val + ":" + applicationHiddenServiceDefinition.getVirtualPort());

            log.info("=================================================");
            log.info("url: {}", httpUrl.orElse("unavailable"));
            log.info("virtual host: {}", applicationHiddenServiceDefinition.getVirtualHost().orElse("unknown"));
            log.info("virtual port: {}", applicationHiddenServiceDefinition.getVirtualPort());
            log.info("host: {}", applicationHiddenServiceDefinition.getHost());
            log.info("port: {}", applicationHiddenServiceDefinition.getPort());
            log.info("directory: {}", applicationHiddenServiceDefinition.getDirectory().getAbsolutePath());
            httpUrl.ifPresent(url -> {
                log.info("-------------------------------------------------");
                try {
                    log.info("run: torsocks -p {} curl {}/index.html -v", tor.getProxy().getPort(), url);
                } catch (TorCtlException e) {
                    log.warn("Could not get tor proxy port");
                }
            });
            log.info("=================================================");
        };
    }

    @Bean
    @Profile("!test")
    public ApplicationRunner torInfoRunner() {
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
