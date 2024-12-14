package io.github.aikusoni.ats.user.api.config;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponseSupport;

import java.util.Optional;

@Slf4j
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
    public HikariDataSource userDbDataSource(
            @Qualifier("vaultTemplateUserDbRole") VaultTemplate vaultTemplateUserDbRole
    ) {
        VaultResponseSupport<DbCredentials> response = vaultTemplateUserDbRole.read(userDbRolePath, DbCredentials.class);
        // Vault에서 접속 정보를 가져오는데 성공한 경우 볼트에서 제공한 정보로 초기화, 아니면 기본 정보로 접속 시도
        DbCredentials dbCredentials = Optional.ofNullable(response)
                .map(VaultResponseSupport::getData)
                .orElse(DbCredentials.builder().username(userDbUsername).password(userDbPassword).build());

        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(userDbUrl)
                .username(dbCredentials.getUsername())
                .password(dbCredentials.getPassword())
                .driverClassName(userDbDriverClassName)
                .build();
    }

    @Data
    @Builder
    public static class DbCredentials {
        private String username;
        private String password;
    }
}
