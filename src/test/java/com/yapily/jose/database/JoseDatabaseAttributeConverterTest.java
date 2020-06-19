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
import org.springframework.core.io.ClassPathResource;
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
                        .keysPath(new ClassPathResource("keys/"))
                        .jweAlgorithm(JWEAlgorithm.RSA_OAEP_256.getName())
                        .jwsAlgorithm(JWSAlgorithm.RS256.getName())
                        .tokenFormat(JoseDatabaseTokenFormat.JWS_JWE)
                        .encryptionMethod(EncryptionMethod.A256CBC_HS512.getName())
                        .build())),
                Arguments.of(new JoseDatabaseConfig(JoseDatabaseConfigurationProperties.builder()
                        .keysPath(new ClassPathResource("keys/"))
                        .jweAlgorithm(JWEAlgorithm.RSA_OAEP_256.getName())
                        .jwsAlgorithm(JWSAlgorithm.RS256.getName())
                        .tokenFormat(JoseDatabaseTokenFormat.JWS)
                        .encryptionMethod(EncryptionMethod.A256CBC_HS512.getName())
                        .build())),
                Arguments.of(new JoseDatabaseConfig(JoseDatabaseConfigurationProperties.builder()
                        .keysPath(new ClassPathResource("keys/"))
                        .jweAlgorithm(JWEAlgorithm.RSA_OAEP_256.getName())
                        .jwsAlgorithm(JWSAlgorithm.RS256.getName())
                        .tokenFormat(JoseDatabaseTokenFormat.JWE_JWS)
                        .encryptionMethod(EncryptionMethod.A256CBC_HS512.getName())
                        .build())),
                Arguments.of(new JoseDatabaseConfig(JoseDatabaseConfigurationProperties.builder()
                        .keysPath(new ClassPathResource("keys/"))
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

    @Test
    void convertToEntityAttributeAnInvalidFormatToken() throws Exception {
        JoseDatabaseConfig config = new JoseDatabaseConfig(JoseDatabaseConfigurationProperties.builder()
                .keysPath(new ClassPathResource("keys/"))
                .jweAlgorithm(JWEAlgorithm.RSA_OAEP_256.getName())
                .jwsAlgorithm(JWSAlgorithm.RS256.getName())
                .tokenFormat(JoseDatabaseTokenFormat.JWS_JWE)
                .encryptionMethod(EncryptionMethod.A256CBC_HS512.getName())
                .build());
        JoseDatabaseJWTService joseDatabaseJWTService = new JoseDatabaseJWTService(config);
        JoseDatabaseAttributeConverter joseDatabaseAttributeConverter = new JoseDatabaseAttributeConverter(config, joseDatabaseJWTService);

        assertThrows(JoseDatabaseException.class, () -> {
            joseDatabaseAttributeConverter.convertToEntityAttribute("invalid-token");
        });
    }

    @Test
    void convertToEntityAttributeAnInvalidSignatureToken() throws Exception {
        JoseDatabaseConfig config = new JoseDatabaseConfig(JoseDatabaseConfigurationProperties.builder()
                .keysPath(new ClassPathResource("keys/"))
                .jweAlgorithm(JWEAlgorithm.RSA_OAEP_256.getName())
                .jwsAlgorithm(JWSAlgorithm.RS256.getName())
                .tokenFormat(JoseDatabaseTokenFormat.JWS_JWE)
                .encryptionMethod(EncryptionMethod.A256CBC_HS512.getName())
                .build());
        JoseDatabaseJWTService joseDatabaseJWTService = new JoseDatabaseJWTService(config);
        JoseDatabaseAttributeConverter joseDatabaseAttributeConverter = new JoseDatabaseAttributeConverter(config, joseDatabaseJWTService);

        assertThrows(JoseDatabaseException.class, () -> {
            joseDatabaseAttributeConverter.convertToEntityAttribute("eyJraWQiOiJ2YWxpZC1zaWduaW5nLWtleSIsImFsZyI6IlBTNTEyIn0.ZXlKcmFXUWlPaUoyWVd4cFpDMWxibU55ZVhCMGFXOXVMV3RsZVNJc0ltVnVZeUk2SWtFeU5UWkhRMDBpTENKaGJHY2lPaUpTVTBFdFQwRkZVQzB5TlRZaWZRLm45ZnMzVTl4YWYtLS1MQjBZVWlLdmVZWEZsMU9HcHBDTWxtREVxMnFkU1daR0RjZVk3NWpkX3dvdTdVOWo4Z0RNbUlFN3Z0Z2tXeWdxQlIzcFlOWUJyRzFVVEp2SVFRMWdPY1Y2TmtXbmN2ZlZyNWw2MEI2cHFQWi0wWHc3Z2w4UW9fVjZURWFyRjhMYi05RlZ2RGVpMDY4eHprXzZzSUlNcWFLallOT3RUNGV0S3BNR2MtQ3ZDRmlveWlEQjR3WmlJWTNSWUp1ZkJsbFV6UlY4emtaeWhWMVN5SkIya2p5aERtT0Z1UFJRampmc0o5aHhsRGVuM3dBeFctcy1mclFGb0t0RVFfSjBzVGswSVFVclJhY2Z0bWRFeEdDQjNka21XYk5jWnVac1M2bFM0YTRTMk4zOG9Zcnp0MnlaMWlEYm4tUUhjekhTanJKcm5rcDRnWUxndy5QZ2FhMVFlVzNLLTNMUVFLLnhUaUdWZnVPdFEuY3JhSU0ybWZDbHRTWlpBXzVpaGxxQQ.invalidsignature");
        });
    }

    @Test
    void convertToEntityAttributeAnUnknownKey() throws Exception {
        JoseDatabaseConfig config = new JoseDatabaseConfig(JoseDatabaseConfigurationProperties.builder()
                .keysPath(new ClassPathResource("keys/"))
                .jweAlgorithm(JWEAlgorithm.RSA_OAEP_256.getName())
                .jwsAlgorithm(JWSAlgorithm.RS256.getName())
                .tokenFormat(JoseDatabaseTokenFormat.JWS_JWE)
                .encryptionMethod(EncryptionMethod.A256CBC_HS512.getName())
                .build());
        JoseDatabaseJWTService joseDatabaseJWTService = new JoseDatabaseJWTService(config);
        JoseDatabaseAttributeConverter joseDatabaseAttributeConverter = new JoseDatabaseAttributeConverter(config, joseDatabaseJWTService);

        assertThrows(IllegalStateException.class, () -> {
            joseDatabaseAttributeConverter.convertToEntityAttribute("eyJhbGciOiJIUzI1NiIsImtpZCI6InVua25vd24ifQ.ZXlKcmFXUWlPaUoyWVd4cFpDMWxibU55ZVhCMGFXOXVMV3RsZVNJc0ltVnVZeUk2SWtFeU5UWkhRMDBpTENKaGJHY2lPaUpTVTBFdFQwRkZVQzB5TlRZaWZRLm45ZnMzVTl4YWYtLS1MQjBZVWlLdmVZWEZsMU9HcHBDTWxtREVxMnFkU1daR0RjZVk3NWpkX3dvdTdVOWo4Z0RNbUlFN3Z0Z2tXeWdxQlIzcFlOWUJyRzFVVEp2SVFRMWdPY1Y2TmtXbmN2ZlZyNWw2MEI2cHFQWi0wWHc3Z2w4UW9fVjZURWFyRjhMYi05RlZ2RGVpMDY4eHprXzZzSUlNcWFLallOT3RUNGV0S3BNR2MtQ3ZDRmlveWlEQjR3WmlJWTNSWUp1ZkJsbFV6UlY4emtaeWhWMVN5SkIya2p5aERtT0Z1UFJRampmc0o5aHhsRGVuM3dBeFctcy1mclFGb0t0RVFfSjBzVGswSVFVclJhY2Z0bWRFeEdDQjNka21XYk5jWnVac1M2bFM0YTRTMk4zOG9Zcnp0MnlaMWlEYm4tUUhjekhTanJKcm5rcDRnWUxndy5QZ2FhMVFlVzNLLTNMUVFLLnhUaUdWZnVPdFEuY3JhSU0ybWZDbHRTWlpBXzVpaGxxQQ.n3wVbDKcCGuDItQ6ct00L3Iq8-Q_PdSm956IhyJHIzKy_vgkODimEVmIO8hH_3VYaTTOa-Kgswg1W1k-b2UHIDLNZqrqDFjPXYjT70b0rSkGmWqtjvtrrfMPszsmdQkdFcqTV7GtZb6K-Kw4bZBao3_MZN1DYLNa8uDfiC-3ccZVCKHohY4awf6o6bvZHR6JzlSEgv0srjaZjsl5o5GsYoES2RJr9SqHp7-Vy9DRrdTiqtaNOyYNAp3MmK3KbaWvripyCgqd5U-5idWPgQ9KJNzPSAu1M2tEAaOXUH3lmmjOiOU_ldca0AnWtN0zBOarcKN-ArViRL-fPMv2t0BwcQ");
        });
    }
}