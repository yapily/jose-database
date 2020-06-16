package com.yapily.jose.database;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
/**
 * The Pojo of the custom actuator endpoint. Gives information about what JOSE settings is in place and the current keys loaded
 */
public class JoseDatabaseActuatorResponse {

    //On all details
    private JWKSet validKeys;
    private JWKSet expiredKeys;
    private JWKSet revokedKeys;

    private JWK currentEncryptionKey;
    private JWK currentSigningKey;
    private EncryptionMethod encryptionMethod;

    //On short view
    private Map<JoseDatabaseKeyStatus, List<String>> keyIDsByStatus;
}
