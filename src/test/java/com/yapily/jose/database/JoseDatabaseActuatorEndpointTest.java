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
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@EnableConfigurationProperties(value = JoseDatabaseConfigurationProperties.class)
@ComponentScan(basePackages = {"com.yapily"})
@SpringBootTest(classes = {JoseDatabaseConfigurationProperties.class, JoseDatabaseAttributeConverter.class, JoseDatabaseActuatorEndpoint.class})
class JoseDatabaseActuatorEndpointTest {

    @Autowired
    private JoseDatabaseActuatorEndpoint joseDatabaseActuatorEndpoint;

    @Test
    public void testActuatorShowDetails() throws Exception {
        JoseDatabaseConfig joseDatabaseConfig = new JoseDatabaseConfig(JoseDatabaseConfigurationProperties.builder()
                .keysPath(new ClassPathResource("keys/"))
                .tokenFormat(JoseDatabaseTokenFormat.JWS_JWE)
                .encryptionMethod(EncryptionMethod.A256CBC_HS512.getName())
                .actuator(JoseDatabaseConfigurationProperties.Actuator.builder()
                        .showDetails(true)
                        .enabled(true)
                        .build())
                .build());

        JoseDatabaseActuatorResponse actuatorResponse = new JoseDatabaseActuatorEndpoint(joseDatabaseConfig).actuator();
        assertThat(actuatorResponse.getCurrentEncryptionKey()).isNotNull();
        assertThat(actuatorResponse.getCurrentEncryptionKey().isPrivate()).isEqualTo(false);

        assertThat(actuatorResponse.getCurrentSigningKey()).isNotNull();
        assertThat(actuatorResponse.getCurrentSigningKey().isPrivate()).isEqualTo(false);

        assertThat(actuatorResponse.getValidKeys()).isNotNull();
        assertThat(actuatorResponse.getValidKeys().getKeys().size()).isNotZero();
        assertThat(actuatorResponse.getValidKeys().getKeys().get(0).isPrivate()).isEqualTo(false);

        assertThat(actuatorResponse.getExpiredKeys()).isNotNull();
        assertThat(actuatorResponse.getExpiredKeys().getKeys().size()).isNotZero();
        assertThat(actuatorResponse.getExpiredKeys().getKeys().get(0).isPrivate()).isEqualTo(false);

        assertThat(actuatorResponse.getRevokedKeys()).isNotNull();
        assertThat(actuatorResponse.getRevokedKeys().getKeys().size()).isNotZero();
        assertThat(actuatorResponse.getRevokedKeys().getKeys().get(0).isPrivate()).isEqualTo(false);

        assertThat(actuatorResponse.getEncryptionMethod()).isNotNull();
        assertThat(actuatorResponse.getKeyIDsByStatus()).isNullOrEmpty();

    }

    @Test
    public void testActuatorShowMin() throws Exception {
        JoseDatabaseConfig joseDatabaseConfig = new JoseDatabaseConfig(JoseDatabaseConfigurationProperties.builder()
                .keysPath(new ClassPathResource("keys/"))
                .tokenFormat(JoseDatabaseTokenFormat.JWS_JWE)
                .encryptionMethod(EncryptionMethod.A256CBC_HS512.getName())
                .actuator(JoseDatabaseConfigurationProperties.Actuator.builder()
                        .showDetails(false)
                        .enabled(true)
                        .build())
                .build());

        JoseDatabaseActuatorResponse actuatorResponse = new JoseDatabaseActuatorEndpoint(joseDatabaseConfig).actuator();
        assertThat(actuatorResponse.getCurrentEncryptionKey()).isNull();

        assertThat(actuatorResponse.getCurrentSigningKey()).isNull();

        assertThat(actuatorResponse.getValidKeys()).isNull();

        assertThat(actuatorResponse.getExpiredKeys()).isNull();

        assertThat(actuatorResponse.getRevokedKeys()).isNull();

        assertThat(actuatorResponse.getEncryptionMethod()).isNotNull();
        assertThat(actuatorResponse.getKeyIDsByStatus()).isNotNull();

    }
}