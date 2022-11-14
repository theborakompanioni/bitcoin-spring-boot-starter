package org.tbk.bitcoin.fee.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Slf4j
@Configuration(proxyBeanMethods = false)
@Import(CustomMempoolSpaceFeeProviderConfig.class)
class BitcoinFeeExampleApplicationConfig {
}
