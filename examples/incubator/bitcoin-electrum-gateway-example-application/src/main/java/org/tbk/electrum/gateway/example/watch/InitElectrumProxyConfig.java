package org.tbk.electrum.gateway.example.watch;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.ConfigKeyEnum;
import org.tbk.spring.testcontainer.core.MoreTestcontainers;
import org.tbk.spring.testcontainer.tor.TorContainer;

import static org.tbk.spring.testcontainer.core.MoreTestcontainers.buildInternalContainerUrl;

@Slf4j
@RequiredArgsConstructor
public class InitElectrumProxyConfig implements InitializingBean {

    @NonNull
    private final ElectrumClient client;

    @NonNull
    private final TorContainer<?> torContainer;

    @Override
    public void afterPropertiesSet() {
        log.info("Setting up tor proxy for electrum...");
        this.client.setConfig(ConfigKeyEnum.network_proxy_enabled, Boolean.TRUE.toString());
        this.client.setConfig(ConfigKeyEnum.network_proxy, buildInternalContainerUrl(torContainer, "socks5", 9050));
        this.client.setConfig(ConfigKeyEnum.network_proxy_user, "");
        this.client.setConfig(ConfigKeyEnum.network_proxy_password, "");

        printConfig(ConfigKeyEnum.network_proxy_enabled);
        printConfig(ConfigKeyEnum.network_proxy);
    }

    private void printConfig(ConfigKeyEnum key) {
        log.info("config '{}': {}", key.getKey().getKey(), this.client.getConfig(key).orElse(null));
    }
}