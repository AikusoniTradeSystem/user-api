package io.github.aikusoni.ats.user.api.config;

import com.zaxxer.hikari.HikariDataSource;
import io.github.aikusoni.ats.spring.core.exception.ATSRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.lease.domain.RequestedSecret;
import org.springframework.vault.core.lease.event.SecretLeaseCreatedEvent;
import org.springframework.vault.core.lease.event.SecretLeaseExpiredEvent;

import java.util.Optional;

import static io.github.aikusoni.ats.user.api.constants.UserApiErrorCode.DATASOURCE_SECRET_UPDATE_FAILED;

@Slf4j
@Component
public class VaultDatabaseLeaseEventListener {

    @Autowired
    private HikariDataSource userDbDataSource;

    @Value("${vault.user-db-role.role-path}")
    private String userDbRolePath;

    private HikariDataSource dataSource(String leasePath) {
        if (leasePath.equals(userDbRolePath)) {
            return userDbDataSource;
        }
        return null;
    }

    @EventListener
    public void onLeaseCreated(SecretLeaseCreatedEvent event) {
        Optional.of(event)
                .map(SecretLeaseCreatedEvent::getSource)
                .map(RequestedSecret::getPath)
                .map(this::dataSource)
                .ifPresent(dataSource -> updateDataSource(event, dataSource));
    }

    @EventListener
    public void onLeaseExpired(SecretLeaseExpiredEvent event) {

    }

    // 데이터소스 갱신 메서드
    private void updateDataSource(SecretLeaseCreatedEvent event, HikariDataSource dataSource) {
        String newUsername = Optional.ofNullable(event)
                .map(SecretLeaseCreatedEvent::getSecrets)
                .map(secrets -> secrets.get("username"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .orElseThrow(() -> new ATSRuntimeException(DATASOURCE_SECRET_UPDATE_FAILED, "VDL-000001", "Failed to update datasource secret"));
        String newPassword = Optional.ofNullable(event)
                .map(SecretLeaseCreatedEvent::getSecrets)
                .map(secrets -> secrets.get("password"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .orElseThrow(() -> new ATSRuntimeException(DATASOURCE_SECRET_UPDATE_FAILED, "VDL-000001", "Failed to update datasource secret"));

        dataSource.setUsername(newUsername);
        dataSource.setPassword(newPassword);
        dataSource.getHikariPoolMXBean().softEvictConnections(); // 기존 연결을 소프트 방식으로 종료하여 새로운 연결 생성
    }
}
