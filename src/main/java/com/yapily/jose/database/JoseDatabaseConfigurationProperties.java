package com.yapily.jose.database;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "jose-database")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Pojo properties that load the configuration from the yaml
 */
public class JoseDatabaseConfigurationProperties {

    private String keysPath = "/";
    private Actuator actuator;
    private JoseDatabaseTokenFormat tokenFormat = JoseDatabaseTokenFormat.JWS_JWE;
    private String jwsAlgorithm = JWSAlgorithm.PS512.getName();
    private String jweAlgorithm = JWEAlgorithm.RSA_OAEP_256.getName();
    private String encryptionMethod = EncryptionMethod.A256CBC_HS512.getName();

    public String validKeysJsonPath() {
        return keysPath + "/valid-keys.json";
    }

    public String expiredKeysJsonPath() {
        return keysPath + "/expired-keys.json";
    }

    public String revokedKeysJsonPath() {
        return keysPath + "/revoked-keys.json";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Actuator {
        private boolean enabled = false;
        private boolean showDetails = false;
    }
}
