package org.tbk.bitcoin.example.payreq;

import com.google.common.io.CharStreams;
import org.consensusj.bitcoin.jsonrpc.BitcoinExtendedClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.message.GetInfoResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.tbk.bitcoin.regtest.BitcoindRegtestTestHelper;
import org.tbk.lightning.lnd.grpc.config.LndClientAutoConfigProperties;
import org.tbk.spring.testcontainer.lnd.LndContainer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@EnableScheduling
@Configuration(proxyBeanMethods = false)
public class BitcoinPaymentRequestExampleApplicationConfig {

    /**
     * We must have access to a wallet for "getnewaddress" command to work.
     * Create a wallet if none is found (currently only when in regtest mode)!
     * Maybe move to {@link org.tbk.bitcoin.regtest.config.BitcoinRegtestAutoConfiguration}?
     */
    @Bean
    public InitializingBean createWalletIfMissing(BitcoinExtendedClient bitcoinRegtestClient) {
        return () -> BitcoindRegtestTestHelper.createDefaultWalletIfNecessary(bitcoinRegtestClient);
    }

    @Bean
    @Profile("!test")
    public ApplicationRunner mainRunner(LndClientAutoConfigProperties properties,
                                        LndContainer<?> lndContainer,
                                        SynchronousLndAPI lndApi) {
        return args -> {
            String lndConnectString = new LndConnect(properties, lndContainer).getValue();

            GetInfoResponse info = lndApi.getInfo();
            log.info("=================================================");
            log.info("[lnd] lndconnect: {}", lndConnectString);
            log.info("=================================================");
            log.info("[lnd] identity_pubkey: {}", info.getIdentityPubkey());
            log.info("[lnd] alias: {}", info.getAlias());
            log.info("[lnd] version: {}", info.getVersion());
        };
    }

    private static class LndConnect {

        @Getter
        private final String value;

        LndConnect(LndClientAutoConfigProperties properties,
                   LndContainer<?> lndContainer) {
            this.value = buildLndConnectString(properties, lndContainer);
        }

        // see https://github.com/LN-Zap/lndconnect/blob/master/lnd_connect_uri.md
        private static String buildLndConnectString(LndClientAutoConfigProperties properties,
                                                    LndContainer<?> lndContainer) {
            String cert = certFromContainer(properties, lndContainer);
            String macaroon = macaroonFromContainer(properties, lndContainer);

            String ip = "127.0.0.1";
            Integer port = lndContainer.getMappedPort(properties.getRpcport());
            return String.format("lndconnect://%s:%d?cert=%s&macaroon=%s", ip, port, cert, macaroon);
        }

        private static String macaroonFromContainer(LndClientAutoConfigProperties properties,
                                                    LndContainer<?> lndContainer) {
            return lndContainer.copyFileFromContainer(properties.getMacaroonFilePath(), inputStream -> {
                byte[] macaroon = IOUtils.toByteArray(inputStream);
                return Base64.getUrlEncoder().encodeToString(macaroon);
            });
        }

        private static String certFromContainer(LndClientAutoConfigProperties properties,
                                                LndContainer<?> lndContainer) {
            return lndContainer.copyFileFromContainer(properties.getCertFilePath(), inputStream -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    List<String> lines = CharStreams.readLines(reader);

                    // remove '-----BEGIN CERTIFICATE-----', '-----END CERTIFICATE-----' and line breaks
                    String certBase64 = lines.stream()
                            .skip(1)
                            .limit(lines.size() - 2)
                            .collect(Collectors.joining());

                    return Base64.getUrlEncoder().encodeToString(Base64.getDecoder().decode(certBase64));
                }
            });
        }
    }
}
