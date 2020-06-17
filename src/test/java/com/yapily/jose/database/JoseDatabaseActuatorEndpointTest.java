package com.yapily.jose.database;

import com.nimbusds.jose.EncryptionMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
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
                .keysPath("keys")
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
                .keysPath("keys")
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