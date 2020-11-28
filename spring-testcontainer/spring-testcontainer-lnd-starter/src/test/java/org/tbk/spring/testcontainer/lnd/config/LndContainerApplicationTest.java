package org.tbk.spring.testcontainer.lnd.config;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lightningj.lnd.wrapper.MacaroonContext;
import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.ValidationException;
import org.lightningj.lnd.wrapper.message.Chain;
import org.lightningj.lnd.wrapper.message.GetInfoResponse;
import org.lightningj.lnd.wrapper.message.NetworkInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.tbk.spring.testcontainer.lnd.LndContainer;

import javax.net.ssl.SSLException;
import javax.xml.bind.DatatypeConverter;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class LndContainerApplicationTest {

    @SpringBootApplication
    public static class LndContainerTestApplication {

        public static void main(String[] args) {
            new SpringApplicationBuilder()
                    .sources(LndContainerTestApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);
        }
    }

    @Autowired(required = false)
    @Qualifier("lndContainer")
    private LndContainer<?> lndContainer;

    @Test
    public void contextLoads() {
        assertThat(lndContainer, is(notNullValue()));
    }

    @Test
    public void itShouldBeCompatibleWithLightningJ() throws StatusException, ValidationException {
        assertThat(lndContainer, is(notNullValue()));

        String certFileInContainer = "/lnd/.lnd/tls.cert";
        String macaroonFileInContainer = "/lnd/.lnd/data/chain/bitcoin/regtest/admin.macaroon";

        String host = lndContainer.getHost();
        Integer mappedPort = lndContainer.getMappedPort(10009);

        SslContext sslContext = lndContainer.copyFileFromContainer(certFileInContainer, inputStream -> {
            return GrpcSslContexts.configure(SslContextBuilder.forClient(), SslProvider.OPENSSL)
                    .trustManager(inputStream)
                    .build();
        });
        MacaroonContext macaroonContext = lndContainer.copyFileFromContainer(macaroonFileInContainer, inputStream -> {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            String hex = DatatypeConverter.printHexBinary(bytes);
            return () -> hex;
        });

        SynchronousLndAPI lndApi = new SynchronousLndAPI(host, mappedPort, sslContext, macaroonContext);

        assertThat(lndApi, is(notNullValue()));

        GetInfoResponse info = lndApi.getInfo();
        assertThat(info, is(notNullValue()));
        assertThat(info.getVersion(), startsWith("0.11.1-beta"));
        assertThat(info.getAlias(), is("tbk-lnd-testcontainer-regtest"));
        assertThat(info.getNumActiveChannels(), is(0));

        assertThat(info.getChains(), hasSize(1));
        Chain chain = info.getChains().stream().findFirst().orElseThrow();
        assertThat(chain.getNetwork(), is("regtest"));

        NetworkInfo networkInfo = lndApi.getNetworkInfo();
        assertThat(networkInfo, is(notNullValue()));
        assertThat("node is running alone in the network", networkInfo.getNumNodes(), is(1));

    }
}

