/**
 * Copyright 2020 Yapily
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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