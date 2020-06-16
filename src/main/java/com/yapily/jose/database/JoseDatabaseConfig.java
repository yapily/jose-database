package com.yapily.jose.database;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.KeyUse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.security.Security;
import java.util.Optional;

@Configuration
@Slf4j
@Getter
/**
 * Load the properties file and the necessary items, like the keys.
 */
public class JoseDatabaseConfig {

    private JoseDatabaseConfigurationProperties joseDatabaseConfigurationProperties;

    private final JWKSet validJwkSet;
    private final JWKSet expiredJwkSet;
    private final JWKSet revokedJwkSet;

    private final Optional<JWK> currentEncryptionKey;
    private final Optional<JWK> currentSigningKey;

    @Value("${foo}")
    private String foo;

    public JoseDatabaseConfig(JoseDatabaseConfigurationProperties joseDatabaseConfigurationProperties) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        this.joseDatabaseConfigurationProperties = joseDatabaseConfigurationProperties;

        File validKeysFile = new ClassPathResource(joseDatabaseConfigurationProperties.validKeysJsonPath()).getFile();
        if (!validKeysFile.exists()) {
            log.error("Valid keys Json file '{}' doesn't exist", joseDatabaseConfigurationProperties.validKeysJsonPath());
            throw new IllegalStateException("Valid keys Json file '" + joseDatabaseConfigurationProperties.validKeysJsonPath() + "' doesn't exist");
        }
        validJwkSet = JWKSet.load(validKeysFile);

        File expiredKeysFile = new ClassPathResource(joseDatabaseConfigurationProperties.expiredKeysJsonPath()).getFile();
        if (!expiredKeysFile.exists()) {
            log.warn("Expired keys Json file '{}' doesn't exist", joseDatabaseConfigurationProperties.expiredKeysJsonPath());
            expiredJwkSet = new JWKSet();
        } else {
            expiredJwkSet = JWKSet.load(expiredKeysFile);
        }

        File revokedKeysFile = new ClassPathResource(joseDatabaseConfigurationProperties.revokedKeysJsonPath()).getFile();
        if (!revokedKeysFile.exists()) {
            log.warn("Revoked keys Json file '{}' doesn't exist", joseDatabaseConfigurationProperties.revokedKeysJsonPath());
            revokedJwkSet = new JWKSet();
        } else {
            revokedJwkSet = JWKSet.load(revokedKeysFile);
        }

        log.info("Expired keys: {}", expiredJwkSet.toPublicJWKSet());
        log.info("Valid keys: {}", validJwkSet.toPublicJWKSet());
        log.info("Revoked keys: {}", revokedJwkSet.toPublicJWKSet());

        currentEncryptionKey = validJwkSet.getKeys().stream().filter(k -> k.getKeyUse() == KeyUse.ENCRYPTION).findFirst();
        currentSigningKey = validJwkSet.getKeys().stream().filter(k -> k.getKeyUse() == KeyUse.SIGNATURE).findFirst();

        log.info("Token format configured: {}", joseDatabaseConfigurationProperties.getTokenFormat());
        switch (joseDatabaseConfigurationProperties.getTokenFormat()) {

            case JWS_JWE:
            case JWE_JWS:
            case JWE:
                if (currentEncryptionKey.isEmpty()) {
                    log.error("No encryption keys found");
                    throw new IllegalStateException("No encryption key found in '" + joseDatabaseConfigurationProperties.validKeysJsonPath()+ "'");
                }
                if (currentEncryptionKey.get().getKeyType() != KeyType.EC && currentEncryptionKey.get().getKeyType() != KeyType.RSA) {
                    throw new IllegalStateException("Key type '" + currentEncryptionKey.get().getKeyType() + "' of key '"
                            + currentEncryptionKey.get().getKeyID() + "' not supported");
                }
                log.info("Encryption key selected for encrypting fields: {}", currentEncryptionKey.get().toPublicJWK());
                break;
            case JWS:
                //No need of an encryption key
                break;
        }

        switch (joseDatabaseConfigurationProperties.getTokenFormat()) {
            case JWS_JWE:
            case JWE_JWS:
            case JWS:
                if (currentSigningKey.isEmpty()) {
                    log.error("No signing keys found");
                    throw new IllegalStateException("No signing key found in '" + joseDatabaseConfigurationProperties.validKeysJsonPath()+ "'");
                }
                if (currentSigningKey.get().getKeyType() != KeyType.EC && currentSigningKey.get().getKeyType() != KeyType.RSA) {
                    throw new IllegalStateException("Key type '" + currentSigningKey.get().getKeyType() + "' of key '"
                            + currentSigningKey.get().getKeyID() + "' not supported");
                }
                log.info("Signing key selected for signing fields: {}", currentSigningKey.get().toPublicJWK());
                break;
            case JWE:
                //No need of a signing key
                break;
        }
    }

    public JoseDatabaseConfigurationProperties.Actuator getActuator() {
        return joseDatabaseConfigurationProperties.getActuator();
    }

    public JoseDatabaseTokenFormat getTokenFormat() {
        return joseDatabaseConfigurationProperties.getTokenFormat();
    }

    public JWSAlgorithm getJwsAlgorithm() {
        return JWSAlgorithm.parse(joseDatabaseConfigurationProperties.getJwsAlgorithm());
    }

    public JWEAlgorithm getJweAlgorithm() {
        return JWEAlgorithm.parse(joseDatabaseConfigurationProperties.getJweAlgorithm());
    }

    public EncryptionMethod getEncryptionMethod() {
        return EncryptionMethod.parse(joseDatabaseConfigurationProperties.getEncryptionMethod());
    }
}
