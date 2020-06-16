package com.yapily.jose.database;

import com.nimbusds.jose.jwk.JWK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@Endpoint(id = "jose-database")
@ConditionalOnProperty(
        value="jose-database.actuator.enabled",
        havingValue = "true")
/**
 * Expose an actuator endpoint to help you identify the current JOSE settings in place.
 * In particular, you can tell if your service has the expected keys in place. Handy for key rotation.
 */
public class JoseDatabaseActuatorEndpoint {

    private JoseDatabaseConfig joseDatabaseConfig;

    public JoseDatabaseActuatorEndpoint(JoseDatabaseConfig joseDatabaseConfig) {
        this.joseDatabaseConfig = joseDatabaseConfig;
    }

    @ReadOperation
    public JoseDatabaseActuatorResponse actuator() {
        JoseDatabaseActuatorResponse.JoseDatabaseActuatorResponseBuilder builder = JoseDatabaseActuatorResponse.builder();

        if (joseDatabaseConfig.getActuator().isShowDetails()) {
            builder
                    .validKeys(joseDatabaseConfig.getValidJwkSet().toPublicJWKSet())
                    .expiredKeys(joseDatabaseConfig.getExpiredJwkSet().toPublicJWKSet())
                    .revokedKeys(joseDatabaseConfig.getRevokedJwkSet().toPublicJWKSet());

            joseDatabaseConfig.getCurrentEncryptionKey().ifPresent(builder::currentEncryptionKey);
            joseDatabaseConfig.getCurrentSigningKey().ifPresent(builder::currentSigningKey);
        } else {
            Map<JoseDatabaseKeyStatus, List<String>> keysByStatus = new HashMap<>();
            keysByStatus.put(JoseDatabaseKeyStatus.VALID, joseDatabaseConfig.getValidJwkSet().getKeys().stream().map(JWK::getKeyID).collect(Collectors.toList()));
            keysByStatus.put(JoseDatabaseKeyStatus.EXPIRED, joseDatabaseConfig.getExpiredJwkSet().getKeys().stream().map(JWK::getKeyID).collect(Collectors.toList()));
            keysByStatus.put(JoseDatabaseKeyStatus.REVOKED, joseDatabaseConfig.getRevokedJwkSet().getKeys().stream().map(JWK::getKeyID).collect(Collectors.toList()));

            builder.keyIDsByStatus(keysByStatus);
        }
        builder.encryptionMethod(joseDatabaseConfig.getEncryptionMethod());
        return builder.build();
    }
}