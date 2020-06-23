/**
 * Copyright 2020 Yapily
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.yapily.jose.database.serialisers.nimbus;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.util.JSONObjectUtils;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class JWKSetDeserializer extends StdDeserializer<JWKSet> {

    private JWKDeserializer jwkDeserializer = new JWKDeserializer();

    public JWKSetDeserializer() {
        this(null);
    }

    public JWKSetDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public JWKSet deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        String jwkSetSerialised = jsonParser.readValueAsTree().toString();
        try {
            return parse(JSONObjectUtils.parse(jwkSetSerialised));
        } catch (ParseException e) {
            log.error("can't deserialize JWK set {}", jwkSetSerialised, e);
            return null;
        }
    }

    public JWKSet parse(JSONObject json) throws ParseException {
        JSONArray keyArray = JSONObjectUtils.getJSONArray(json, "keys");
        List<JWK> keys = new LinkedList();

        for (int i = 0; i < keyArray.size(); ++i) {
            if (!(keyArray.get(i) instanceof JSONObject)) {
                throw new ParseException("The \"keys\" JSON array must contain JSON objects only", 0);
            }

            JSONObject keyJSON = (JSONObject) keyArray.get(i);

            try {
                keyJSON.entrySet().removeAll(
                        keyJSON.entrySet().stream()
                               .filter(e -> e.getValue() == null)
                               .collect(Collectors.toSet())
                );

                keys.add(JWK.parse(keyJSON));
            } catch (ParseException var6) {
                throw new ParseException("Invalid JWK at position " + i + ": " + var6.getMessage(), 0);
            }
        }

        Map<String, Object> additionalMembers = new HashMap();
        Iterator var8 = json.entrySet().iterator();

        while (var8.hasNext()) {
            Map.Entry<String, Object> entry = (Map.Entry) var8.next();
            if (entry.getKey() != null && !entry.getKey().equals("keys")) {
                additionalMembers.put(entry.getKey(), entry.getValue());
            }
        }

        return new JWKSet(keys, additionalMembers);
    }
}
