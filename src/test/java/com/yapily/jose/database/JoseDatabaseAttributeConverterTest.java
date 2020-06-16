package com.yapily.jose.database;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.text.ParseException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@EnableConfigurationProperties(value = JoseDatabaseConfigurationProperties.class)
@ComponentScan(basePackages = {"com.yapily"})
@SpringBootTest(classes = {JoseDatabaseConfigurationProperties.class, JoseDatabaseAttributeConverter.class, JoseDatabaseJWTService.class})
class JoseDatabaseAttributeConverterTest {

    @Autowired
    private JoseDatabaseAttributeConverter joseDatabaseAttributeConverter;
    @Autowired
    private JoseDatabaseJWTService joseDatabaseJWTService;

    private String attribute = "TheDude";
    @Test
    void convertToDatabaseColumn() throws ParseException, JOSEException {
        String attributeAsJWT = joseDatabaseAttributeConverter.convertToDatabaseColumn(attribute);
        String resultingAttribute = joseDatabaseJWTService.decryptJWE(joseDatabaseJWTService.verifyJWS(attributeAsJWT));//No parse exception means it's indeed a JWE(JWS)
        assertThat(resultingAttribute).isEqualTo(attribute);
    }

    @Test
    void convertToEntityAttribute() {
        String resultingAttribute = joseDatabaseAttributeConverter.convertToEntityAttribute(joseDatabaseAttributeConverter.convertToDatabaseColumn(attribute));
        assertThat(resultingAttribute).isEqualTo(attribute);
    }
}