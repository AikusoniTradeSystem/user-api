package io.github.aikusoni.ats.user.api.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Configuration
public class VaultConfig {

    @Value("${vault.uri}")
    private String vaultUri;

    // AppRole 1 설정
    @Value("${vault.user-db-role.role-id}")
    private String userDbRoleId;

    @Value("${vault.user-db-role.secret-id}")
    private String userDbSecretId;

    @Value("${vault.ssl.trust-store}")
    private String trustStorePath;

    @Value("${vault.ssl.trust-store-password}")
    private String trustStorePassword;

    @Value("${vault.ssl.ignore-trust-store}")
    private boolean ignoreTrustStore;

    @Bean
    public VaultTemplate vaultTemplateUserDbRole() {
        try {
            RestTemplate restTemplate = restTemplate();
            VaultEndpoint endpoint = VaultEndpoint.from(URI.create(vaultUri));
            AppRoleAuthenticationOptions options = AppRoleAuthenticationOptions.builder()
                    .roleId(AppRoleAuthenticationOptions.RoleId.provided(userDbRoleId))
                    .secretId(AppRoleAuthenticationOptions.SecretId.provided(userDbSecretId))
                    .build();

            String healthUrl = endpoint.createUriString("/sys/health");
            Map<String, Object> healthMap = restTemplate.getForObject(healthUrl, Map.class);

            Boolean isInitialized = (Boolean) healthMap.get("initialized");
            Boolean isSealed = (Boolean) healthMap.get("sealed");
            Integer serverTime = (Integer) healthMap.get("server_time_utc");

            if (isInitialized && !isSealed) {
                log.debug("Vault is initialized and unsealed. Server time: {}", serverTime);
                AppRoleAuthentication auth = new AppRoleAuthentication(options, restTemplate);
                auth.login();
                return new VaultTemplate(endpoint, auth);
            } else {
                throw new IllegalStateException("Vault is not in a usable state. Sealed or uninitialized.");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to connect to Vault server", e);
        }
    }

    public RestTemplate restTemplate() throws Exception {
        if (ignoreTrustStore) {
            return restTemplateWithoutTrustStore();
        } else {
            return restTemplateWithTrustStore();
        }
    }

    public RestTemplate restTemplateWithoutTrustStore() throws Exception {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(acceptingTrustStrategy)
                .build();

        SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext)
                .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplateBuilder().requestFactory(() -> factory).build();
    }

    public RestTemplate restTemplateWithTrustStore() throws Exception {
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(new File(trustStorePath), Optional.ofNullable(trustStorePassword).map(String::toCharArray).orElse(null))
                .build();

        SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext)
                .build();

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }
}
