package org.tbk.spring.testcontainer.tor;

import org.apache.http.HttpHost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.*;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public final class TorHttpClientBuilder {
    public static HttpClientBuilder custom(Proxy proxy) {
        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new ProxySelectorPlainConnectionSocketFactory(proxy))
                .register("https", new ProxySelectorSSLConnectionSocketFactory(proxy, SSLContexts.createSystemDefault()))
                .build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);
        return HttpClients.custom()
                .setConnectionManager(cm);
    }

    private static class ProxySelectorPlainConnectionSocketFactory implements ConnectionSocketFactory {

        private final Proxy proxy;

        ProxySelectorPlainConnectionSocketFactory(Proxy proxy) {
            this.proxy = requireNonNull(proxy);
        }

        @Override
        public Socket createSocket(HttpContext context) {
            return TorHttpClientBuilder.createSocket(context, this.proxy);
        }

        @Override
        public Socket connectSocket(int connectTimeout, Socket sock, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context) throws IOException {
            return PlainConnectionSocketFactory.INSTANCE.connectSocket(connectTimeout, sock, host, remoteAddress, localAddress, context);
        }
    }

    private static final class ProxySelectorSSLConnectionSocketFactory extends SSLConnectionSocketFactory {
        private final Proxy proxy;

        ProxySelectorSSLConnectionSocketFactory(Proxy proxy, SSLContext sslContext) {
            super(sslContext);
            this.proxy = requireNonNull(proxy);
        }

        @Override
        public Socket createSocket(HttpContext context) {
            return TorHttpClientBuilder.createSocket(context, this.proxy);
        }
    }

    private static Socket createSocket(HttpContext context, Proxy proxyOrNull) {
        HttpHost httpTargetHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
        URI uri = URI.create(httpTargetHost.toURI());

        Proxy proxy = Optional.ofNullable(proxyOrNull)
                .orElseGet(() -> ProxySelector.getDefault().select(uri).iterator().next());

        return new Socket(proxy);
    }
}