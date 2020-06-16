package com.yapily.jose.database;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@EnableConfigurationProperties(value = JoseDatabaseConfigurationProperties.class)
@ComponentScan(basePackages = {"com.yapily"})
@SpringBootTest(classes = {JoseDatabaseConfigurationProperties.class, JoseDatabaseJWTService.class, JoseDatabaseConfig.class})
class JoseDatabaseJWTServiceTest {
    @Autowired
    private JoseDatabaseJWTService joseDatabaseJWTService;
    @Autowired
    private JoseDatabaseConfig joseDatabaseConfig;

    private String payload = "Hello, world!";

    @Test
    void encryptPayload() throws JOSEException, ParseException {

        JWEObject jwe = JWEObject.parse(joseDatabaseJWTService.encryptPayload(payload));
        jwe.decrypt(new RSADecrypter((RSAKey) joseDatabaseConfig.getCurrentEncryptionKey().get()));
        assertEquals(payload, jwe.getPayload().toString());
    }

    @Test
    void decryptJWE() throws JOSEException, ParseException {
        assertEquals(payload, joseDatabaseJWTService.decryptJWE((joseDatabaseJWTService.encryptPayload(payload))));
    }

    @Test
    void signPayload() throws JOSEException, ParseException {
        JWSObject jwsObject = JWSObject.parse(joseDatabaseJWTService.signPayload(payload));

        JWSVerifier verifier = new RSASSAVerifier((RSAKey) joseDatabaseConfig.getCurrentSigningKey().get());

        assertTrue(jwsObject.verify(verifier));
    }

    @Test
    void verifyJWS() throws JOSEException, ParseException {
        assertThat(joseDatabaseJWTService.verifyJWS((joseDatabaseJWTService.signPayload(payload)))).isEqualTo(payload);
    }
}