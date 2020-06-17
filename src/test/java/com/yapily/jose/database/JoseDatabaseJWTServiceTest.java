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

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@EnableConfigurationProperties(value = JoseDatabaseConfigurationProperties.class)
@ComponentScan(basePackages = {"com.yapily"})
@SpringBootTest(classes = {JoseDatabaseConfigurationProperties.class, JoseDatabaseJWTService.class, JoseDatabaseConfig.class})
class JoseDatabaseJWTServiceTest {

    private String payload = "Hello, world!";

    private static Stream<Arguments> provideJoseDatabaseConfigs() throws Exception {
        return Stream.of(
                Arguments.of(new JoseDatabaseConfig(JoseDatabaseConfigurationProperties.builder()
                        .keysPath("keys-rsa")
                        .jweAlgorithm(JWEAlgorithm.RSA_OAEP_256.getName())
                        .jwsAlgorithm(JWSAlgorithm.RS256.getName())
                        .tokenFormat(JoseDatabaseTokenFormat.JWS_JWE)
                        .encryptionMethod(EncryptionMethod.A256CBC_HS512.getName())
                        .build())),
                Arguments.of(new JoseDatabaseConfig(JoseDatabaseConfigurationProperties.builder()
                        .keysPath("keys-ec")
                        .jweAlgorithm(JWEAlgorithm.ECDH_ES_A256KW.getName())
                        .jwsAlgorithm(JWSAlgorithm.ES256.getName())
                        .tokenFormat(JoseDatabaseTokenFormat.JWS_JWE)
                        .encryptionMethod(EncryptionMethod.A256CBC_HS512.getName())
                        .build()))
        );
    }

    @ParameterizedTest
    @MethodSource("provideJoseDatabaseConfigs")
    void encryptPayload(JoseDatabaseConfig config) throws JOSEException, ParseException {
        JoseDatabaseJWTService joseDatabaseJWTService = new JoseDatabaseJWTService(config);

        JWEObject jwe = JWEObject.parse(joseDatabaseJWTService.encryptPayload(payload));
        if (config.getKeysPath().equals("keys-rsa")) {
            jwe.decrypt(new RSADecrypter((RSAKey) config.getCurrentEncryptionKey().get()));
        } else {
            jwe.decrypt(new ECDHDecrypter((ECKey) config.getCurrentEncryptionKey().get()));
        }
        assertEquals(payload, jwe.getPayload().toString());
    }

    @ParameterizedTest
    @MethodSource("provideJoseDatabaseConfigs")
    void decryptJWE(JoseDatabaseConfig config) throws JOSEException, ParseException {
        JoseDatabaseJWTService joseDatabaseJWTService = new JoseDatabaseJWTService(config);
        assertEquals(payload, joseDatabaseJWTService.decryptJWE((joseDatabaseJWTService.encryptPayload(payload))));
    }

    @ParameterizedTest
    @MethodSource("provideJoseDatabaseConfigs")
    void signPayload(JoseDatabaseConfig config) throws JOSEException, ParseException {
        JoseDatabaseJWTService joseDatabaseJWTService = new JoseDatabaseJWTService(config);

        JWSObject jwsObject = JWSObject.parse(joseDatabaseJWTService.signPayload(payload));

        JWSVerifier verifier;
        if (config.getKeysPath().equals("keys-rsa")) {
            verifier = new RSASSAVerifier((RSAKey) config.getCurrentSigningKey().get());
        } else {
            verifier = new ECDSAVerifier((ECKey) config.getCurrentSigningKey().get());
        }
        assertTrue(jwsObject.verify(verifier));
    }

    @ParameterizedTest
    @MethodSource("provideJoseDatabaseConfigs")
    void verifyJWS(JoseDatabaseConfig config) throws JOSEException, ParseException {
        JoseDatabaseJWTService joseDatabaseJWTService = new JoseDatabaseJWTService(config);

        assertThat(joseDatabaseJWTService.verifyJWS((joseDatabaseJWTService.signPayload(payload)))).isEqualTo(payload);
    }
}