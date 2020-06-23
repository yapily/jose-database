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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.yapily.jose.database.serialisers.nimbus.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
/**
 * The Pojo of the custom actuator endpoint. Gives information about what JOSE settings is in place and the current keys loaded
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JoseDatabaseActuatorResponse {

    //On all details
    @JsonSerialize(using = JWKSetSerializer.class)
    @JsonDeserialize(using = JWKSetDeserializer.class)
    private JWKSet validKeys;
    @JsonSerialize(using = JWKSetSerializer.class)
    @JsonDeserialize(using = JWKSetDeserializer.class)
    private JWKSet expiredKeys;
    @JsonSerialize(using = JWKSetSerializer.class)
    @JsonDeserialize(using = JWKSetDeserializer.class)
    private JWKSet revokedKeys;

    @JsonSerialize(using = JWKSerializer.class)
    @JsonDeserialize(using = JWKDeserializer.class)
    private JWK currentEncryptionKey;
    @JsonSerialize(using = JWKSerializer.class)
    @JsonDeserialize(using = JWKDeserializer.class)
    private JWK currentSigningKey;
    @JsonSerialize(using = EncryptionMethodSerializer.class)
    @JsonDeserialize(using = EncryptionMethodDeserializer.class)
    private EncryptionMethod encryptionMethod;

    //On short view
    private Map<JoseDatabaseKeyStatus, List<String>> keyIDsByStatus;
}
