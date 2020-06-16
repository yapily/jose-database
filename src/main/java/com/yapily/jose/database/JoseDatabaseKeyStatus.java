package com.yapily.jose.database;

/**
 * The different status of a key
 */
public enum JoseDatabaseKeyStatus {

    /**
     * The key is valid and should be used for encrypting (JWE) or signing (JWS)
     */
    VALID,

    /**
     * The key is no longer valid, although it can still be used for decrypting JWE or verifying JWS
     */
    EXPIRED,

    /**
     * The key has been revoked and should not be used anymore. It serves as history and can be used for audit reason.
     */
    REVOKED
}
