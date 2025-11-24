package org.grnet.status.dtos.tenant;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "TenantRequestDto", description = "Represents the configuration of a tenant.")
public class TenantRequestDto {

    @Schema(
            type = SchemaType.OBJECT,
            implementation = TenantInfoDto.class,
            description = "Tenant Info "
    )
    @JsonProperty("info")
    @Valid
    public TenantInfoDto info;
}