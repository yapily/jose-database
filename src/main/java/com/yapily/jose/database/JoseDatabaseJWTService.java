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
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
@Slf4j
/**
 * Jose service layer on top of the nimbus library, to facilitate the creating of JWE/JWS
 */
public class JoseDatabaseJWTService {

    private final JoseDatabaseConfig joseDatabaseConfig;

    public JoseDatabaseJWTService(JoseDatabaseConfig joseDatabaseConfig) {
        this.joseDatabaseConfig = joseDatabaseConfig;
    }

    /**
     * Encrypt the given payload into a JWE (https://tools.ietf.org/html/rfc7516), using the settings loaded from the configuration file
     * @param payloadSerialised the payload serialised as a string
     * @return a JWE serialised as a String
     * @throws JOSEException Issue raised by the nimbus library, when trying to create the JWE
     */
    public String encryptPayload(String payloadSerialised) throws JOSEException {
        JWK encryptionKey = joseDatabaseConfig.getCurrentEncryptionKey().get();
        JWEAlgorithm jweAlgorithm = joseDatabaseConfig.getJweAlgorithm();
        EncryptionMethod encryptionMethod = joseDatabaseConfig.getEncryptionMethod();
        if (encryptionKey.getAlgorithm() != null && encryptionKey.getAlgorithm() instanceof JWEAlgorithm) {
            jweAlgorithm = (JWEAlgorithm) encryptionKey.getAlgorithm();
            log.debug("Algorithm defined in encryption key '{}' and is a JWE algorithm '{}'", encryptionKey.toPublicJWK(), encryptionKey.getAlgorithm());
        }

        JWEHeader header = new JWEHeader.Builder(jweAlgorithm, encryptionMethod)
                .keyID(encryptionKey.getKeyID())
                .build();
        Payload payload = new Payload(payloadSerialised);
        // Create the JWE object and encrypt it
        JWEObject jwe;
        if (encryptionKey.getKeyType() == KeyType.EC) {
            ECKey ecKey = (ECKey) encryptionKey;
            jwe = new JWEObject(header, payload);
            jwe.encrypt(new ECDHEncrypter(ecKey));
        } else if (encryptionKey.getKeyType() == KeyType.RSA) {
            RSAKey rsaKey = (RSAKey) encryptionKey;
            jwe = new JWEObject(header, payload);
            jwe.encrypt(new RSAEncrypter(rsaKey));
        } else {
            throw new IllegalStateException("Key type '" + encryptionKey.getKeyType() + "' of key '" + encryptionKey.getKeyID() + "' not supported");
        }
        return jwe.serialize();
    }

    /**
     * Decrypt the given JWE (https://tools.ietf.org/html/rfc7516) and return the String serialised payload
     * @param jweSerialised the JWE serialised as a String
     * @return the String serialised payload inside the JWE
     * @throws JOSEException Issue raised by the nimbus library, when trying to create the JWE
     * @throws ParseException Issue raised by the nimbus library, when trying to parse the serialised JWE
     */
    public String decryptJWE(String jweSerialised) throws JOSEException, ParseException {
        JWEObject jwe = JWEObject.parse(jweSerialised);
        JWK keyByKeyId = joseDatabaseConfig.getValidJwkSet().getKeyByKeyId(jwe.getHeader().getKeyID());
        if (keyByKeyId == null) {
            keyByKeyId = joseDatabaseConfig.getExpiredJwkSet().getKeyByKeyId(jwe.getHeader().getKeyID());
        }
        if (keyByKeyId == null) {
            throw new IllegalStateException("Attribute '" + jweSerialised + "' encrypted with an unknown key '"
                    + jwe.getHeader().getKeyID() + "'");
        }
        if (keyByKeyId.getKeyType() == KeyType.EC) {
            ECKey ecKey = (ECKey) keyByKeyId;
            jwe.decrypt(new ECDHDecrypter(ecKey));
        } else if (keyByKeyId.getKeyType() == KeyType.RSA) {
            RSAKey rsaKey = (RSAKey) keyByKeyId;
            jwe.decrypt(new RSADecrypter(rsaKey));
        }
        return jwe.getPayload().toString();
    }

    /**
     * Sign the given String serialised payload into a JWS https://tools.ietf.org/html/rfc7515 , based on the configuration settings
     * @param payloadSerialised the payload String serialised
     * @return the JWS containing the payload
     * @throws JOSEException Issue raised by the nimbus library, when trying to create the JWE
     */
    public String signPayload(String payloadSerialised) throws JOSEException {
        JWK signingKey = joseDatabaseConfig.getCurrentSigningKey().get();
        JWSAlgorithm jwsAlgorithm = joseDatabaseConfig.getJwsAlgorithm();
        if (signingKey.getAlgorithm() != null && signingKey.getAlgorithm() instanceof JWSAlgorithm) {
            jwsAlgorithm = (JWSAlgorithm) signingKey.getAlgorithm();
            log.debug("Algorithm defined in signing key '{}' and is a JWS algorithm '{}'", signingKey.toPublicJWK(), signingKey.getAlgorithm());
        }

        Payload payload = new Payload(payloadSerialised);
        JWSHeader header = new JWSHeader.Builder(jwsAlgorithm)
                .keyID(signingKey.getKeyID())
                .build();

        // Create the JWE object and encrypt it
        JWSObject jws;
        if (signingKey.getKeyType() == KeyType.EC) {
            ECKey ecKey = (ECKey) signingKey;
            jws = new JWSObject(header, payload);
            jws.sign(new ECDSASigner(ecKey));
        } else if (signingKey.getKeyType() == KeyType.RSA) {
            RSAKey rsaKey = (RSAKey) signingKey;
            jws = new JWSObject(header, payload);
            jws.sign(new RSASSASigner(rsaKey));
        } else {
            throw new IllegalStateException("Key type '" + signingKey.getKeyType() + "' of key '" + signingKey.getKeyID() + "' not supported");
        }
        return jws.serialize();
    }

    /**
     * Verify the JWS with the keys loaded from the configuration settings
     * @param jwsSerialised the String serialised JWS
     * @return the String serialised payload that is in the JWS. Will return an exception if the signature wasn't valid
     * @throws JOSEException Issue raised by the nimbus library, when trying to create the JWE
     * @throws ParseException Issue raised by the nimbus library, when trying to parse the serialised JWE
     */
    public String verifyJWS(String jwsSerialised) throws ParseException, JOSEException {
        JWSObject jws = JWSObject.parse(jwsSerialised);
        JWK keyByKeyId = joseDatabaseConfig.getValidJwkSet().getKeyByKeyId(jws.getHeader().getKeyID());
        if (keyByKeyId == null) {
            keyByKeyId = joseDatabaseConfig.getExpiredJwkSet().getKeyByKeyId(jws.getHeader().getKeyID());
        }
        if (keyByKeyId == null) {
            throw new IllegalStateException("Attribute '" + jwsSerialised + "' encrypted with an unknown key '"
                    + jws.getHeader().getKeyID() + "'");
        }
        JWSVerifier jwsVerifier;
        if (keyByKeyId.getKeyType() == KeyType.EC) {
            ECKey ecKey = (ECKey) keyByKeyId;
            jwsVerifier = new ECDSAVerifier(ecKey);
        } else if (keyByKeyId.getKeyType() == KeyType.RSA) {
            RSAKey rsaKey = (RSAKey) keyByKeyId;
            jwsVerifier = new RSASSAVerifier(rsaKey);
        } else {
            throw new IllegalStateException("Unsupported key type '" + keyByKeyId.getKeyType() + "'");
        }
        if (!jws.verify(jwsVerifier)) {
            throw new JOSEException("Invalid signature for token '" + jwsSerialised + "'.");
        }

        return jws.getPayload().toString();
    }
}
