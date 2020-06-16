package com.yapily.jose.database;

/**
 * The different formats of JWT
 */
public enum JoseDatabaseTokenFormat {
    /**
     * A JWS, containing as a payload a JWE. The JWE contains the actual String payload we want to store securely.
     * In other word: JWS(JWE("The Value we want to store securely"))
     */
    JWS_JWE,

    /**
     * A JWE, containing as a payload a JWS. The JWS contains the actual String payload we want to store securely.
     * In other word: JWE(JWS("The Value we want to store securely"))
     */
    JWE_JWS,

    /**
     * A JWS containing the actual String payload we want to store securely.
     * In other word: JWS("The Value we want to store securely")
     */
    JWS,

    /**
     * A JWE containing the actual String payload we want to store securely.
     * In other word: JWE("The Value we want to store securely")
     */
    JWE
}
