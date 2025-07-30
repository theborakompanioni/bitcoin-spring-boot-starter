package org.tbk.electrum.gateway.example.watch;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.tbk.electrum.ElectrumClient;
import org.tbk.electrum.model.ConfigKeyEnum;
import org.tbk.spring.testcontainer.tor.TorContainer;

import static org.tbk.spring.testcontainer.core.MoreTestcontainers.buildInternalContainerUrlWithoutProtocol;

@Slf4j
@RequiredArgsConstructor
public class InitElectrumProxyConfig implements InitializingBean {

    @NonNull
    private final ElectrumClient client;

    @NonNull
    private final TorContainer<?> torContainer;

    @Override
    public void afterPropertiesSet() {
        String proxy = "socks5:%s".formatted(buildInternalContainerUrlWithoutProtocol(torContainer, 9050));
        log.info("Setting up tor proxy for electrum '{}'...", proxy);

        this.client.setConfig(ConfigKeyEnum.network_proxy_enabled, Boolean.TRUE.toString());
        this.client.setConfig(ConfigKeyEnum.network_proxy, proxy);
        this.client.setConfig(ConfigKeyEnum.network_proxy_user, "");
        this.client.setConfig(ConfigKeyEnum.network_proxy_password, "");
    }
}
