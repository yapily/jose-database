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


import com.nimbusds.jose.EncryptionMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@EnableConfigurationProperties(value = JoseDatabaseConfigurationProperties.class)
@ComponentScan(basePackages = {"com.yapily"})
@SpringBootTest(classes = {JoseDatabaseConfigurationProperties.class})
public class JoseDatabaseConfigTest {

    @Autowired
    private JoseDatabaseConfigurationProperties joseDatabaseConfigurationProperties;

    @Test
    public void testClassicConfiguration() throws Exception {
        JoseDatabaseConfig joseDatabaseConfig = new JoseDatabaseConfig(joseDatabaseConfigurationProperties);
        assertThat(joseDatabaseConfig.getValidJwkSet()).isNotNull();
        assertThat(joseDatabaseConfig.getValidJwkSet().getKeys().size()).isEqualTo(2);
        assertThat(joseDatabaseConfig.getValidJwkSet().getKeys().stream().filter(k -> k.getKeyID().equals("valid-encryption-key")).findAny().isPresent()).isEqualTo(true);
        assertThat(joseDatabaseConfig.getValidJwkSet().getKeys().stream().filter(k -> k.getKeyID().equals("valid-signing-key")).findAny().isPresent()).isEqualTo(true);

        assertThat(joseDatabaseConfig.getExpiredJwkSet()).isNotNull();
        assertThat(joseDatabaseConfig.getExpiredJwkSet().getKeys().size()).isEqualTo(2);
        assertThat(joseDatabaseConfig.getExpiredJwkSet().getKeys().stream().filter(k -> k.getKeyID().equals("expired-encryption-key")).findAny().isPresent()).isEqualTo(true);
        assertThat(joseDatabaseConfig.getExpiredJwkSet().getKeys().stream().filter(k -> k.getKeyID().equals("expired-signing-key")).findAny().isPresent()).isEqualTo(true);

        assertThat(joseDatabaseConfig.getRevokedJwkSet()).isNotNull();
        assertThat(joseDatabaseConfig.getRevokedJwkSet().getKeys().size()).isEqualTo(2);
        assertThat(joseDatabaseConfig.getRevokedJwkSet().getKeys().stream().filter(k -> k.getKeyID().equals("revoked-encryption-key")).findAny().isPresent()).isEqualTo(true);
        assertThat(joseDatabaseConfig.getRevokedJwkSet().getKeys().stream().filter(k -> k.getKeyID().equals("revoked-signing-key")).findAny().isPresent()).isEqualTo(true);

        assertThat(joseDatabaseConfig.getCurrentEncryptionKey().isPresent()).isEqualTo(true);
        assertThat(joseDatabaseConfig.getCurrentSigningKey().isPresent()).isEqualTo(true);

        assertThat(joseDatabaseConfig.getCurrentEncryptionKey().get().getKeyID()).isEqualTo("valid-encryption-key");
        assertThat(joseDatabaseConfig.getCurrentSigningKey().get().getKeyID()).isEqualTo("valid-signing-key");

        assertThat(joseDatabaseConfig.getActuator()).isEqualTo(JoseDatabaseConfigurationProperties.Actuator.builder()
                .enabled(true)
                .showDetails(true)
                .build());

        assertThat(joseDatabaseConfig.getTokenFormat()).isEqualTo(JoseDatabaseTokenFormat.JWS_JWE);
        assertThat(joseDatabaseConfig.getEncryptionMethod()).isEqualTo(EncryptionMethod.A128CBC_HS256);
    }

    @Test
    public void testJWSOnly() throws Exception {
        JoseDatabaseConfig joseDatabaseConfig = new JoseDatabaseConfig(JoseDatabaseConfigurationProperties.builder()
                .keysPath(new ClassPathResource("keys-jws/"))
                .tokenFormat(JoseDatabaseTokenFormat.JWS)
                .build());

        assertThat(joseDatabaseConfig.getTokenFormat()).isEqualTo(JoseDatabaseTokenFormat.JWS);

        assertThat(joseDatabaseConfig.getCurrentEncryptionKey().isPresent()).isEqualTo(false);
        assertThat(joseDatabaseConfig.getCurrentSigningKey().isPresent()).isEqualTo(true);
        assertThat(joseDatabaseConfig.getCurrentSigningKey().get().getKeyID()).isEqualTo("valid-signing-key");
    }
}