package org.tbk.lightning.regtest.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.tbk.lightning.regtest.setup.devel.impl.LocalRegtestLightningNetworkSetupConfig;

@Slf4j
@Configuration(proxyBeanMethods = false)
@Import(LocalRegtestLightningNetworkSetupConfig.class)
class LightningRegtestExampleApplicationConfig {

}
