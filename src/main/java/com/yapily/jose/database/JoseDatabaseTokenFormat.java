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
