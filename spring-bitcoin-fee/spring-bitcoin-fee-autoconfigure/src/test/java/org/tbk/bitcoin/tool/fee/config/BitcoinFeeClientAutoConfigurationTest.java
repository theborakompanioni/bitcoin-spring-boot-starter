package org.tbk.bitcoin.tool.fee.config;

import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class BitcoinFeeClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    /*
    @Test
    public void beansAreCreated() {
        this.contextRunner.withUserConfiguration(BitcoinJsonRpcClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.bitcoin.jsonrpc.network=mainnet",
                        "org.tbk.bitcoin.jsonrpc.rpchost=http://localhost",
                        "org.tbk.bitcoin.jsonrpc.rpcport=7000",
                        "org.tbk.bitcoin.jsonrpc.rpcuser=test",
                        "org.tbk.bitcoin.jsonrpc.rpcpassword=test"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinJsonRpcClientFactory"), is(true));
                    assertThat(context.getBean(BitcoinJsonRpcClientFactory.class), is(notNullValue()));

                    assertThat(context.containsBean("bitcoinJsonRpcClient"), is(true));
                    assertThat(context.getBean(BitcoinClient.class), is(notNullValue()));
                });
    }


    @Test
    public void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(BitcoinJsonRpcClientAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.bitcoin.jsonrpc.enabled=false"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinJsonRpcClientFactory"), is(false));
                    try {
                        context.getBean(BitcoinJsonRpcClientFactory.class);
                        Assert.fail("Should have thrown exception");
                    } catch (NoSuchBeanDefinitionException e) {
                        // continue
                    }

                    assertThat(context.containsBean("bitcoinJsonRpcClient"), is(false));
                    try {
                        context.getBean(BitcoinClient.class);
                        Assert.fail("Should have thrown exception");
                    } catch (NoSuchBeanDefinitionException e) {
                        // continue
                    }
                });
    }*/
}
