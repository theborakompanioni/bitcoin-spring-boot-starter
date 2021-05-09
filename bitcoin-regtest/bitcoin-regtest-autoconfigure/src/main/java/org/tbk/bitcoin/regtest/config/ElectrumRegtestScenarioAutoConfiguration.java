package org.tbk.bitcoin.regtest.config;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.bitcoin.regtest.*;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.ElectrumClientFactory;
import org.tbk.electrum.config.ElectrumDaemonJsonrpcClientAutoConfiguration;
import org.tbk.electrum.config.ElectrumDeamonJsonrpcConfig;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ElectrumClientFactory.class)
@AutoConfigureAfter({
        BitcoinRegtestAutoConfiguration.class,
        ElectrumDaemonJsonrpcClientAutoConfiguration.class
})
public class ElectrumRegtestScenarioAutoConfiguration {

    public static ElectrumClient electrumClient(ElectrumDeamonJsonrpcConfig electrumDeamonJsonrpcConfig,
                                                ElectrumClientFactory factory) {
        URI uri = electrumDeamonJsonrpcConfig.getUri();

        ElectrumClient electrumClient = factory.create(uri, electrumDeamonJsonrpcConfig.getUsername(), electrumDeamonJsonrpcConfig.getPassword());

// TODO: implement "create wallet" rpc call for electrum daemon client!
//  https://github.com/spesmilo/electrum/blob/master/electrum/commands.py#L241 -> electrumClient.loadWallet()


        return electrumClient;
    }
}
