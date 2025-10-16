package org.grnet.status.dtos.encrypt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
@Schema(description = "Request object for encrypting a given secret string.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EncryptRequestDto {

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "The plain text secret to encrypt.")
    @JsonProperty("secret")
    public String secret;
}
