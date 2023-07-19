package org.tbk.bitcoin.tool.fee;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.bitcoin.tool.fee.bitgo.config.BitgoFeeClientAutoConfiguration;
import org.tbk.bitcoin.tool.fee.blockchaininfo.config.BlockchainInfoFeeClientAutoConfiguration;
import org.tbk.bitcoin.tool.fee.config.BitcoinFeeClientAutoConfiguration;
import org.tbk.bitcoin.tool.fee.earndotcom.config.EarndotcomFeeClientAutoConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class CompositeFeeProviderTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void itShouldVerifyThatCompositeProviderHasProviders() {
        this.contextRunner.withUserConfiguration(BitcoinFeeClientAutoConfiguration.class).run(context -> {
            CompositeFeeProvider compositeFeeProvider = context.getBean(CompositeFeeProvider.class);
            assertThat(compositeFeeProvider, is(notNullValue()));

            assertThat(compositeFeeProvider.getProviderCount(), is(0));
        });

        this.contextRunner.withUserConfiguration(
                BitcoinFeeClientAutoConfiguration.class,
                EarndotcomFeeClientAutoConfiguration.class
        ).run(context -> {
            CompositeFeeProvider compositeFeeProvider = context.getBean(CompositeFeeProvider.class);
            assertThat(compositeFeeProvider, is(notNullValue()));

            assertThat(compositeFeeProvider.getProviderCount(), is(1));
        });

        this.contextRunner.withUserConfiguration(
                BitcoinFeeClientAutoConfiguration.class,
                EarndotcomFeeClientAutoConfiguration.class,
                BlockchainInfoFeeClientAutoConfiguration.class,
                BitgoFeeClientAutoConfiguration.class
        ).run(context -> {
            CompositeFeeProvider compositeFeeProvider = context.getBean(CompositeFeeProvider.class);
            assertThat(compositeFeeProvider, is(notNullValue()));

            assertThat(compositeFeeProvider.getProviderCount(), is(3));
        });
    }
}
