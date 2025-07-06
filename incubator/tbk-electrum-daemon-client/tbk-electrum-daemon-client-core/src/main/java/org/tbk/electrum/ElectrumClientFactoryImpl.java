package org.tbk.electrum;

import com.github.arteam.simplejsonrpc.client.JsonRpcClient;
import com.github.arteam.simplejsonrpc.client.Transport;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ElectrumClientFactoryImpl implements ElectrumClientFactory {

    @Override
    public ElectrumClient create(URI uri, String username, String password) {
        CloseableHttpClient httpClient = httpClient();
        Transport transport = transport(httpClient, uri, username, password);
        ElectrumDaemonRpcService delegate = electrumDaemonRpcService(transport);

        return new ElectrumClientImpl(delegate);
    }

    private ElectrumDaemonRpcService electrumDaemonRpcService(Transport transport) {
        return jsonRpcClient(transport).onDemand(ElectrumDaemonRpcService.class);
    }

    private JsonRpcClient jsonRpcClient(Transport transport) {
        return new JsonRpcClient(transport);
    }

    private Transport transport(CloseableHttpClient httpClient, URI uri, String username, String password) {
        Header authHeader = basicAuthHeader(username, password);

        return request -> {
            HttpPost post = new HttpPost(uri);
            post.setHeader(authHeader);
            post.setEntity(new StringEntity(request, StandardCharsets.UTF_8));
            post.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            try (CloseableHttpResponse httpResponse = httpClient.execute(post)) {
                return EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
            }
        };
    }

    private static Header basicAuthHeader(String username, String password) {
        String auth = "%s:%s".formatted(username, password);
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        String authHeaderValue = "Basic %s".formatted(new String(encodedAuth, StandardCharsets.UTF_8));

        return new BasicHeader(HttpHeaders.AUTHORIZATION, authHeaderValue);
    }

    private CloseableHttpClient httpClient() {
        return HttpClientBuilder.create()
                .disableAuthCaching()
                .disableAutomaticRetries()
                .disableRedirectHandling()
                .disableCookieManagement()
                .build();
    }
}
