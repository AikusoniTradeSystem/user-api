package io.github.aikusoni.ats.user.api.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.vault.client.RestTemplateBuilder;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.util.Optional;

public class VaultRestTemplateProvider {

    public static RestTemplate createRestTemplate(VaultEndpoint vaultEndpoint, String trustStorePath, String trustStorePassword) throws Exception {
        return createRestTemplateBuilder(vaultEndpoint, trustStorePath, trustStorePassword).build();
    }

    public static RestTemplateBuilder createRestTemplateBuilder(VaultEndpoint vaultEndpoint, String trustStorePath, String trustStorePassword) throws Exception {
        return RestTemplateBuilder.builder()
                .endpoint(vaultEndpoint)
                .requestFactory(requestFactoryWithTrustStore(trustStorePath, trustStorePassword));
    }

    private static HttpComponentsClientHttpRequestFactory requestFactoryWithTrustStore(String trustStorePath, String trustStorePassword) throws Exception {
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(new File(trustStorePath), Optional.ofNullable(trustStorePassword).map(String::toCharArray).orElse(null))
                .build();

        Registry<ConnectionSocketFactory> socketRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register(URIScheme.HTTPS.getId(), new SSLConnectionSocketFactory(sslContext))
                .register(URIScheme.HTTP.getId(), new PlainConnectionSocketFactory())
                .build();

        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(new PoolingHttpClientConnectionManager(socketRegistry))
                .setConnectionManagerShared(true)
                .build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }
}
