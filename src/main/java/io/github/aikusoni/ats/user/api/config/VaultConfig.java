package io.github.aikusoni.ats.user.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Configuration
public class VaultConfig {

    @Value("${vault.uri}")
    private String vaultUri;

    // AppRole 1 설정
    @Value("${vault.user-db-role.role-id}")
    private String userDbRoleId;

    @Value("${vault.user-db-role.secret-id}")
    private String userDbSecretId;

    @Bean
    public VaultTemplate vaultTemplateUserDbRole() {
        VaultEndpoint endpoint = VaultEndpoint.from(URI.create(vaultUri));
        AppRoleAuthenticationOptions options = AppRoleAuthenticationOptions.builder()
                .roleId(AppRoleAuthenticationOptions.RoleId.provided(userDbRoleId))
                .secretId(AppRoleAuthenticationOptions.SecretId.provided(userDbSecretId))
                .build();
        AppRoleAuthentication auth = new AppRoleAuthentication(options, new RestTemplate());

        return new VaultTemplate(endpoint, auth);
    }
}
