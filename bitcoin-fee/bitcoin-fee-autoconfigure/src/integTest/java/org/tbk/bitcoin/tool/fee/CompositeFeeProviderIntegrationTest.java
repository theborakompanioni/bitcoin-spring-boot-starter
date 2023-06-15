package org.tbk.bitcoin.tool.fee;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.bitcoin.tool.fee.bitgo.config.BitgoFeeClientAutoConfiguration;
import org.tbk.bitcoin.tool.fee.blockchaininfo.config.BlockchainInfoFeeClientAutoConfiguration;
import org.tbk.bitcoin.tool.fee.config.BitcoinFeeClientAutoConfiguration;
import org.tbk.bitcoin.tool.fee.earndotcom.config.EarndotcomFeeClientAutoConfiguration;

import java.time.Duration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
class CompositeFeeProviderIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void itShouldRequestFeeRecommendationSuccessfully() {
        this.contextRunner.withUserConfiguration(
                BitcoinFeeClientAutoConfiguration.class,
                EarndotcomFeeClientAutoConfiguration.class,
                BlockchainInfoFeeClientAutoConfiguration.class,
                BitgoFeeClientAutoConfiguration.class
        ).run(context -> {
            CompositeFeeProvider compositeFeeProvider = context.getBean(CompositeFeeProvider.class);
            assertThat(compositeFeeProvider, is(notNullValue()));

            FeeRecommendationRequest request = FeeRecommendationRequestImpl.builder()
                    .durationTarget(Duration.ofMinutes(30))
                    .build();

            List<FeeRecommendationResponse> response = compositeFeeProvider.request(request)
                    .onErrorContinue((e, o) -> {
                        // fee provider apis might be down temporarily
                        log.warn("Error during fee request", e);
                    })
                    .collectList()
                    .block(Duration.ofSeconds(20));

            // at least one fee provider must answer
            assertThat(response, hasSize(greaterThan(0)));
        });
    }
}
