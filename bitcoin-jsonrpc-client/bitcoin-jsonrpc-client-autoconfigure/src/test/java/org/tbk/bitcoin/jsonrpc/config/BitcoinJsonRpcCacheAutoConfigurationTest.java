package org.tbk.bitcoin.jsonrpc.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

// the "cache" dependency is missing - this is a test to verify the config can handle this
// see the integration tests for a test with the dependency included!
class BitcoinJsonRpcCacheAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void noBeansAreCreatedWhenDependecyIsMissing() {
        this.contextRunner.withUserConfiguration(BitcoinJsonRpcClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.bitcoin.jsonrpc.enabled=true"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinJsonRpcTransactionCache"), is(false));
                    assertThat(context.containsBean("bitcoinJsonRpcRawTransactionInfoCache"), is(false));
                    assertThat(context.containsBean("bitcoinJsonRpcBlockCache"), is(false));
                    assertThat(context.containsBean("bitcoinJsonRpcBlockInfoCache"), is(false));
                    assertThat(context.containsBean("bitcoinJsonRpcCacheFacade"), is(false));
                });
    }
}
