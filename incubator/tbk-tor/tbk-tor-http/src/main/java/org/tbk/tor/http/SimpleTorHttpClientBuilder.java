package org.tbk.tor.http;

import com.runjva.sourceforge.jsocks.protocol.Socks5Proxy;
import org.apache.http.HttpHost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.ssl.SSLContexts;
import org.berndpruenster.netlayer.tor.Tor;
import org.berndpruenster.netlayer.tor.TorCtlException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.*;
import java.util.Optional;

import static java.util.Objects.requireNonNull;


public final class SimpleTorHttpClientBuilder {
    public static HttpClientBuilder tor(Tor tor) throws TorCtlException {
        Socks5Proxy proxy = tor.getProxy();
        InetAddress inetAddress = proxy.getInetAddress();

        SocketAddress socketAddress = new InetSocketAddress(inetAddress, proxy.getPort());

        return custom(new Proxy(Proxy.Type.SOCKS, socketAddress));
    }

    public static HttpClientBuilder custom(Proxy proxy) {
        DefaultHostnameVerifier defaultHostnameVerifier = new DefaultHostnameVerifier();
        SSLContext sslContext = SSLContexts.createSystemDefault();
        ProxySelectorSSLConnectionSocketFactory sslSocketFactory = new ProxySelectorSSLConnectionSocketFactory(proxy, sslContext, defaultHostnameVerifier);
        ProxySelectorPlainConnectionSocketFactory plainSocketFactory = new ProxySelectorPlainConnectionSocketFactory(proxy);

        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", plainSocketFactory)
                .register("https", sslSocketFactory)
                .build();

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg, new FakeDnsResolver());

        return HttpClients.custom()
                .setConnectionManager(cm);
    }

    private static Socket createSocket(HttpContext context, Proxy proxyOrNull) {
        HttpHost httpTargetHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
        URI uri = URI.create(httpTargetHost.toURI());

        Proxy proxy = Optional.ofNullable(proxyOrNull)
                .orElseGet(() -> ProxySelector.getDefault().select(uri).iterator().next());

        return new Socket(proxy);
    }

    /*
     * It is very difficult to say to an apache http client to "not use my DNS servers while connecting through a proxy".
     *
     * Some code taken from:
     * https://stackoverflow.com/questions/22937983/how-to-use-socks-5-proxy-with-apache-http-client-4/25203021#25203021
     */
    private static class ProxySelectorPlainConnectionSocketFactory implements ConnectionSocketFactory {

        private final Proxy proxy;

        ProxySelectorPlainConnectionSocketFactory(Proxy proxy) {
            this.proxy = requireNonNull(proxy);
        }

        @Override
        public Socket createSocket(HttpContext context) {
            return SimpleTorHttpClientBuilder.createSocket(context, this.proxy);
        }

        @Override
        public Socket connectSocket(int connectTimeout, Socket sock, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context) throws IOException {
            InetSocketAddress unresolvedRemote = InetSocketAddress.createUnresolved(host.getHostName(), remoteAddress.getPort());

            return PlainConnectionSocketFactory.INSTANCE.connectSocket(connectTimeout, sock, host, unresolvedRemote, localAddress, context);
        }
    }

    private static final class ProxySelectorSSLConnectionSocketFactory extends SSLConnectionSocketFactory {
        private final Proxy proxy;

        ProxySelectorSSLConnectionSocketFactory(Proxy proxy,
                                                SSLContext sslContext,
                                                HostnameVerifier hostnameVerifier) {
            super(sslContext, hostnameVerifier);
            this.proxy = requireNonNull(proxy);
        }

        @Override
        public Socket createSocket(HttpContext context) {
            return SimpleTorHttpClientBuilder.createSocket(context, this.proxy);
        }

        @Override
        public Socket connectSocket(int connectTimeout, Socket sock, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context) throws IOException {
            InetSocketAddress unresolvedRemote = InetSocketAddress.createUnresolved(host.getHostName(), remoteAddress.getPort());

            return super.connectSocket(connectTimeout, sock, host, unresolvedRemote, localAddress, context);
        }
    }

    static class FakeDnsResolver implements DnsResolver {
        // Return this fake DNS record for every request, we won't be using it
        private static final InetAddress[] fakeDnsEntry = {InetAddress.getLoopbackAddress()};

        @Override
        public InetAddress[] resolve(String host) {
            return fakeDnsEntry;
        }
    }
}