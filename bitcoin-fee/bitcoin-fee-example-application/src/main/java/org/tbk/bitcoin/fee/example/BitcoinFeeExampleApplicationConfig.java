package org.tbk.bitcoin.fee.example;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.tbk.bitcoin.tool.fee.FeeProvider;
import org.tbk.bitcoin.tool.fee.FeeRecommendationRequest;
import org.tbk.bitcoin.tool.fee.FeeRecommendationRequestImpl;
import org.tbk.bitcoin.tool.fee.FeeRecommendationResponse;

import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Configuration(proxyBeanMethods = false)
@Import(CustomMempoolSpaceFeeProviderConfig.class)
class BitcoinFeeExampleApplicationConfig {

    @Bean
    @Profile("!test")
    ApplicationRunner initialFeeRecommendation(@NonNull FeeProvider feeProvider) {
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
                        .onErrorContinue((e, o) -> log.warn("Error on requesting fee recommendation: {}", e.getMessage()))
                        .collectList()
                        .block(Duration.ofSeconds(30));

                log.info("=====================");
                log.info("Duration: {}", duration);

                response.forEach(val -> {
                    log.info("---------------------");
                    log.info("Provider: {}", val.getProviderInfo().getName());
                    val.getFeeRecommendations().forEach(feeRecommendation -> {
                        log.info("Fee: {}", feeRecommendation.getFeeUnit());
                    });
                });
            }

            log.info("=====================");
        };
    }
}
