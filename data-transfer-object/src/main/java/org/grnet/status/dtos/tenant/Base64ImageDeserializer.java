package org.grnet.status.dtos.tenant;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Base64;

public class Base64ImageDeserializer extends JsonDeserializer<byte[]> {
    @Override
    public byte[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dataUrl = p.getText();

        if (dataUrl == null || !dataUrl.startsWith("data:image/")) {
            return null; // or throw exception
        }

        String base64 = dataUrl.substring(dataUrl.indexOf(",") + 1);
        return Base64.getDecoder().decode(base64);
    }
}
