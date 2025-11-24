package org.grnet.status.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.grnet.status.dtos.tenant.TenantInfoDto;
import org.grnet.status.dtos.tenant.TenantPartialResponse;
import org.grnet.status.dtos.tenant.TenantResponseDto;
import org.grnet.status.dtos.tenant.TenantRequestDto;
import org.grnet.status.entities.Tenant;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Mapper(imports = {Timestamp.class, Instant.class})
public interface TenantMapper {

    TenantMapper INSTANCE = Mappers.getMapper(TenantMapper.class);

    @IterableMapping(qualifiedByName = "map")
    List<TenantResponseDto> tenantsToDtos(List<Tenant> tenants);

    @IterableMapping(qualifiedByName = "map1")
    List<TenantPartialResponse> tenantsToPartialDtos(List<Tenant> tenants);

    @Named("map")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "info.name", source = "name")
    @Mapping(target = "info.email", source = "email")
    @Mapping(target = "info.website", source = "website")
    @Mapping(target = "info.description", source = "description")
    @Mapping(target = "info.image", source = "image")
    @Mapping(target = "info.createdAt", source = "createdAt")
    @Mapping(target = "info.updatedAt", source = "updatedAt")
    @Mapping(target = "updatedBy", source = "updatedBy")

    TenantResponseDto tenantToDto(Tenant tenant);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", expression = "java(Timestamp.from(Instant.now()))")
    @Mapping(target = "updatedAt", expression = "java(Timestamp.from(Instant.now()))")
    Tenant dtoToTenant(TenantInfoDto dto);

    // Helper methods for Timestamp <-> Instant mapping
    default Instant map(Timestamp timestamp) {
        return timestamp != null ? timestamp.toInstant() : null;
    }

    default Timestamp map(Instant instant) {
        return instant != null ? Timestamp.from(instant) : null;
    }

    ObjectMapper MAPPER = new ObjectMapper();

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(Timestamp.from(Instant.now()))")
    @Mapping(target = "name", source = "info.name")
    @Mapping(target = "website", source = "info.website")
    @Mapping(target = "image", source = "info.image")
    @Mapping(target = "description", source = "info.description")
    @Mapping(target = "email", source = "info.email")
    void updateToTenant(TenantRequestDto dto, @MappingTarget Tenant tenant);

    @Named("map1")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "updatedBy", source = "updatedBy")

    TenantPartialResponse tenantToPartialDto(Tenant tenant);

}