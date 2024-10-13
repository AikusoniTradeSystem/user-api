package io.github.aikusoni.ats.user.api.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponseSupport;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.user-db.url}")
    private String userDbUrl;

    @Value("${spring.datasource.user-db.username}")
    private String userDbUsername;

    @Value("${spring.datasource.user-db.password}")
    private String userDbPassword;

    @Value("${spring.datasource.user-db.driver-class-name}")
    private String userDbDriverClassName;

    @Value("${vault.user-db-role.role-path}")
    private String userDbRolePath;

    @Primary
    @Bean(name = "userDbDataSource")
    public HikariDataSource userDbDataSource(VaultTemplate vaultTemplateUserDbRole) {
        VaultResponseSupport<DbCredentials> response = vaultTemplateUserDbRole.read(userDbRolePath, DbCredentials.class);
        String username = response.getData().getUsername();
        String password = response.getData().getPassword();

        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(userDbUrl)
                .username(username)
                .password(password)
                .driverClassName(userDbDriverClassName)
                .build();
    }

    @Data
    public static class DbCredentials {
        private String username;
        private String password;
    }
}
