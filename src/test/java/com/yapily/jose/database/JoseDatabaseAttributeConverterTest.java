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

import java.text.ParseException;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@EnableConfigurationProperties(value = JoseDatabaseConfigurationProperties.class)
@ComponentScan(basePackages = {"com.yapily"})
@SpringBootTest(classes = {JoseDatabaseConfigurationProperties.class, JoseDatabaseAttributeConverter.class, JoseDatabaseJWTService.class})
class JoseDatabaseAttributeConverterTest {

    private String attribute = "TheDude";

    private static Stream<Arguments> provideJoseDatabaseConfigs() throws Exception {
        return Stream.of(
                Arguments.of(new JoseDatabaseConfig(JoseDatabaseConfigurationProperties.builder()
                        .keysPath("keys")
                        .jweAlgorithm(JWEAlgorithm.RSA_OAEP_256.getName())
                        .jwsAlgorithm(JWSAlgorithm.RS256.getName())
                        .tokenFormat(JoseDatabaseTokenFormat.JWS_JWE)
                        .encryptionMethod(EncryptionMethod.A256CBC_HS512.getName())
                        .build())),
                Arguments.of(new JoseDatabaseConfig(JoseDatabaseConfigurationProperties.builder()
                        .keysPath("keys")
                        .jweAlgorithm(JWEAlgorithm.RSA_OAEP_256.getName())
                        .jwsAlgorithm(JWSAlgorithm.RS256.getName())
                        .tokenFormat(JoseDatabaseTokenFormat.JWS)
                        .encryptionMethod(EncryptionMethod.A256CBC_HS512.getName())
                        .build())),
                Arguments.of(new JoseDatabaseConfig(JoseDatabaseConfigurationProperties.builder()
                        .keysPath("keys")
                        .jweAlgorithm(JWEAlgorithm.RSA_OAEP_256.getName())
                        .jwsAlgorithm(JWSAlgorithm.RS256.getName())
                        .tokenFormat(JoseDatabaseTokenFormat.JWE_JWS)
                        .encryptionMethod(EncryptionMethod.A256CBC_HS512.getName())
                        .build())),
                Arguments.of(new JoseDatabaseConfig(JoseDatabaseConfigurationProperties.builder()
                        .keysPath("keys")
                        .jweAlgorithm(JWEAlgorithm.RSA_OAEP_256.getName())
                        .jwsAlgorithm(JWSAlgorithm.RS256.getName())
                        .tokenFormat(JoseDatabaseTokenFormat.JWE)
                        .encryptionMethod(EncryptionMethod.A256CBC_HS512.getName())
                        .build()))
        );
    }

    @ParameterizedTest
    @MethodSource("provideJoseDatabaseConfigs")
    void convertToDatabaseColumn(JoseDatabaseConfig config) throws ParseException, JOSEException {
        JoseDatabaseJWTService joseDatabaseJWTService = new JoseDatabaseJWTService(config);
        JoseDatabaseAttributeConverter joseDatabaseAttributeConverter = new JoseDatabaseAttributeConverter(config, joseDatabaseJWTService);

        String attributeAsJWT = joseDatabaseAttributeConverter.convertToDatabaseColumn(attribute);
        String resultingAttribute;
        switch (config.getTokenFormat()) {

            case JWS_JWE:
                resultingAttribute = joseDatabaseJWTService.decryptJWE(joseDatabaseJWTService.verifyJWS(attributeAsJWT));//No parse exception means it's indeed a JWE(JWS)
                break;
            case JWE_JWS:
                resultingAttribute = joseDatabaseJWTService.verifyJWS(joseDatabaseJWTService.decryptJWE(attributeAsJWT));//No parse exception means it's indeed a JWE(JWS)
                break;
            case JWS:
                resultingAttribute = joseDatabaseJWTService.verifyJWS(attributeAsJWT);//No parse exception means it's indeed a JWE(JWS)
                break;
            case JWE:
            default:
                resultingAttribute = joseDatabaseJWTService.decryptJWE(attributeAsJWT);//No parse exception means it's indeed a JWE(JWS)
                break;
        }
        assertThat(resultingAttribute).isEqualTo(attribute);
    }

    @ParameterizedTest
    @MethodSource("provideJoseDatabaseConfigs")
    void convertToEntityAttribute(JoseDatabaseConfig config) {
        JoseDatabaseJWTService joseDatabaseJWTService = new JoseDatabaseJWTService(config);
        JoseDatabaseAttributeConverter joseDatabaseAttributeConverter = new JoseDatabaseAttributeConverter(config, joseDatabaseJWTService);

        String resultingAttribute = joseDatabaseAttributeConverter.convertToEntityAttribute(joseDatabaseAttributeConverter.convertToDatabaseColumn(attribute));
        assertThat(resultingAttribute).isEqualTo(attribute);
    }
}