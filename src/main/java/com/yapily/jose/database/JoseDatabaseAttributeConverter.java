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

import com.nimbusds.jose.JOSEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.AttributeConverter;
import java.text.ParseException;

@Service
@Slf4j
/**
 * Jose Attribute converter. The selected field will be bundle into a JWT.
 */
public class JoseDatabaseAttributeConverter implements AttributeConverter<String, String> {

    private JoseDatabaseConfig joseDatabaseConfig;
    private JoseDatabaseJWTService joseDatabaseJWTService;

    /**
     * Constructor
     * @param joseDatabaseConfig the configuration loaded based on the yaml properties file
     * @param joseDatabaseJWTService the JOSE service
     */
    public JoseDatabaseAttributeConverter(JoseDatabaseConfig joseDatabaseConfig, JoseDatabaseJWTService joseDatabaseJWTService) {
        this.joseDatabaseConfig = joseDatabaseConfig;
        this.joseDatabaseJWTService = joseDatabaseJWTService;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            switch (joseDatabaseConfig.getTokenFormat()) {
                case JWS_JWE:
                    return joseDatabaseJWTService.signPayload(joseDatabaseJWTService.encryptPayload(attribute));
                case JWE_JWS:
                    return joseDatabaseJWTService.encryptPayload(joseDatabaseJWTService.signPayload(attribute));
                case JWS:
                    return joseDatabaseJWTService.signPayload(attribute);
                case JWE:
                default:
                    return joseDatabaseJWTService.encryptPayload(attribute);
            }
        } catch (JOSEException e) {
            log.error("Couldn't encrypt/sign the attribute", e);
            throw new JoseDatabaseException(e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            switch (joseDatabaseConfig.getTokenFormat()) {
                case JWS_JWE:
                    return joseDatabaseJWTService.decryptJWE(joseDatabaseJWTService.verifyJWS(dbData));
                case JWE_JWS:
                    return joseDatabaseJWTService.verifyJWS(joseDatabaseJWTService.decryptJWE(dbData));
                case JWS:
                    return joseDatabaseJWTService.verifyJWS(dbData);
                case JWE:
                default:
                    return joseDatabaseJWTService.decryptJWE(dbData);
            }
        } catch (ParseException e) {
            log.error("Couldn't parse the attribute '{}'", dbData, e);
            throw new JoseDatabaseException(e);
        } catch (JOSEException e) {
            log.error("Couldn't decrypt/verify the attribute '{}'", dbData, e);
            throw new JoseDatabaseException(e);
        }
    }
}