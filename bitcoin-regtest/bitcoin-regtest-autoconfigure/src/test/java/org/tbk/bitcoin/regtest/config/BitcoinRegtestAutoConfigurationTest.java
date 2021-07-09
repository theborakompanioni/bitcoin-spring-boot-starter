package org.tbk.bitcoin.regtest.config;

import com.msgilligan.bitcoinj.rpc.BitcoinExtendedClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.tbk.bitcoin.jsonrpc.config.BitcoinJsonRpcClientAutoConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BitcoinRegtestAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void beansAreCreated() {
        this.contextRunner.withUserConfiguration(BitcoinJsonRpcClientAutoConfiguration.class,
                BitcoinRegtestAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.bitcoin.regtest.enabled=true",
                        "org.tbk.bitcoin.jsonrpc.network=regtest",
                        "org.tbk.bitcoin.jsonrpc.rpchost=http://localhost",
                        "org.tbk.bitcoin.jsonrpc.rpcport=13337",
                        "org.tbk.bitcoin.jsonrpc.rpcuser=test",
                        "org.tbk.bitcoin.jsonrpc.rpcpassword=test"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinRegtestClient"), is(true));
                    assertThat(context.getBean(BitcoinExtendedClient.class), is(notNullValue()));
                });
    }


    @Test
    public void noBeansAreCreated() {
        this.contextRunner.withUserConfiguration(BitcoinJsonRpcClientAutoConfiguration.class,
                BitcoinRegtestAutoConfiguration.class)
                .withPropertyValues(
                        "org.tbk.bitcoin.regtest.enabled=false",
                        "org.tbk.bitcoin.jsonrpc.network=regtest",
                        "org.tbk.bitcoin.jsonrpc.rpchost=http://localhost",
                        "org.tbk.bitcoin.jsonrpc.rpcport=13337",
                        "org.tbk.bitcoin.jsonrpc.rpcuser=test",
                        "org.tbk.bitcoin.jsonrpc.rpcpassword=test"
                )
                .run(context -> {
                    assertThat(context.containsBean("bitcoinRegtestClient"), is(false));
                    assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean(BitcoinExtendedClient.class));
                });
    }
}
