package org.tbk.bitcoin.fee.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.context.WebServerPortFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.tbk.bitcoin.tool.fee.FeeProvider;
import org.tbk.bitcoin.tool.fee.FeeRecommendationRequest;
import org.tbk.bitcoin.tool.fee.FeeRecommendationRequestImpl;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponse;

import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@SpringBootApplication(proxyBeanMethods = false)
public class BitcoinFeeExampleApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(BitcoinFeeExampleApplication.class)
                .listeners(applicationPidFileWriter(), webServerPortFileWriter())
                .web(WebApplicationType.SERVLET)
                .profiles("development", "local", "demo")
                .run(args);
    }

    public static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }

    public static ApplicationListener<?> webServerPortFileWriter() {
        return new WebServerPortFileWriter("application.port");
    }

    @Bean
    @Profile("demo-disabled-for-now")
    ApplicationRunner mainRunner(FeeProvider feeProvider) {
        return args -> {
            log.info("=====================");
            List<Duration> durations = IntStream.range(0, 12)
                    .mapToObj(val -> Duration.ofMinutes(val * 30L))
                    .toList();

            for (Duration duration : durations) {
                FeeRecommendationRequest request = FeeRecommendationRequestImpl.builder()
                        .durationTarget(duration)
                        .build();

                List<FeeRecommendationResponse> response = feeProvider.request(request)
                        .collectList()
                        .block(Duration.ofSeconds(30));

                log.info("=====================");
                log.info("Duration: {}", duration);

                response.forEach(val -> {
                    log.info("=====================");
                    log.info("{}", val);
                });
            }

            log.info("=====================");
        };
    }
}
