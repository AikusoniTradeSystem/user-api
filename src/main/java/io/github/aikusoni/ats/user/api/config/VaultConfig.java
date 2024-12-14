package io.github.aikusoni.ats.user.api.config;

import io.github.aikusoni.ats.user.api.constants.UserApiConstants;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions;
import org.springframework.vault.authentication.SessionManager;
import org.springframework.vault.authentication.SimpleSessionManager;
import org.springframework.vault.client.RestTemplateBuilder;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.SslConfiguration;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Optional;

import static io.github.aikusoni.ats.user.api.config.VaultRestTemplateProvider.createRestTemplate;
import static io.github.aikusoni.ats.user.api.config.VaultRestTemplateProvider.createRestTemplateBuilder;
import static io.github.aikusoni.ats.user.api.constants.UserApiConstants.REST_TEMPLATE_MODE_WITHOUT_TRUST_STORE;
import static io.github.aikusoni.ats.user.api.constants.UserApiConstants.REST_TEMPLATE_MODE_WITH_TRUST_STORE;

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

    @Value("${vault.user-db-role.trust-store}")
    private String userDbRoleTrustStorePath;

    @Value("${vault.user-db-role.trust-store-password}")
    private String userDbRoleTrustStorePassword;

    @Bean(name = "vaultTemplateUserDbRole")
    public VaultTemplate vaultTemplateUserDbRole() {
        try {
            if (isVaultUsable()) {
                VaultEndpoint endpoint = endpoint();
                RestTemplateBuilder restTemplateBuilder = createRestTemplateBuilder(endpoint, userDbRoleTrustStorePath, userDbRoleTrustStorePassword);
                AppRoleAuthenticationOptions options = AppRoleAuthenticationOptions.builder()
                        .roleId(AppRoleAuthenticationOptions.RoleId.provided(userDbRoleId))
                        .secretId(AppRoleAuthenticationOptions.SecretId.provided(userDbSecretId))
                        .build();

                AppRoleAuthentication auth = new AppRoleAuthentication(options, restTemplateBuilder.build());
                auth.login();
                SessionManager sessionManager = new SimpleSessionManager(auth);

                return new VaultTemplate(
                        restTemplateBuilder
                        , sessionManager
                );
            } else {
                throw new IllegalStateException("Vault is not in a usable state. Sealed or uninitialized.");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to connect to Vault server", e);
        }
    }

    private boolean isVaultUsable() {
        try {
            VaultEndpoint endpoint = endpoint();
            RestTemplate restTemplate = createRestTemplate(endpoint(), userDbRoleTrustStorePath, userDbRoleTrustStorePassword);

            String healthUrl = endpoint.createUriString("/sys/health");
            Map<String, Object> healthMap = restTemplate.getForObject(healthUrl, Map.class);

            Boolean isInitialized = (Boolean) healthMap.get("initialized");
            Boolean isSealed = (Boolean) healthMap.get("sealed");
//            Integer serverTime = (Integer) healthMap.get("server_time_utc");

            return isInitialized && !isSealed;
        } catch (Exception e) {
            log.error("Unable to connect to Vault server", e);
            return false;
        }
    }

    private VaultEndpoint endpoint() {
        return VaultEndpoint.from(URI.create(vaultUri));
    }
}
