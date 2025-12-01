package org.grnet.status.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.grnet.status.dtos.tenant.*;
import org.grnet.status.entities.Tenant;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(imports = {Timestamp.class, Instant.class})
public interface TenantMapper {

    TenantMapper INSTANCE = Mappers.getMapper(TenantMapper.class);
    static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));


    default List<TenantResponseDto> webApiTenantsToDtos(
            List<Tenant> tenants,
            List<TenantWebApiGetResponse> webApiGetResponses
    ) {
        List<TenantResponseDto> dtos = new ArrayList<>();

        // Build map: tenantId -> Tenant
        Map<String, Tenant> tenantMap = tenants.stream()
                .collect(Collectors.toMap(t -> t.id, t -> t));

        for (TenantWebApiGetResponse response : webApiGetResponses) {

            // Ensure response contains data
            if (response.getData() == null || response.getData().isEmpty()) {
                continue;
            }

            var item = response.getData().get(0); // id + info
            var tenant = tenantMap.get(item.getId());

            if (tenant != null) {
                dtos.add(webApiTenantToDto(tenant, item.getInfo()));
            }
        }

        return dtos;
    }

    @Named("map")
    default TenantResponseDto webApiTenantToDto(Tenant tenant, TenantWebApiGetResponse.Info info) {
        TenantResponseDto dto = new TenantResponseDto();
        dto.id = tenant.id;
        TenantWebApiRequest.TenantWebApiInfo dtoInfo = new TenantWebApiRequest.TenantWebApiInfo();
        dtoInfo.name = info.getName();
        dtoInfo.email = info.getEmail();
        dtoInfo.website = info.getWebsite();
        dtoInfo.description = info.getDescription();
        dtoInfo.image = info.getImage();
        dtoInfo.createdAt = Instant.from(DATE_TIME_FMT.parse(info.getCreated()));
        dtoInfo.updatedAt = Instant.from(DATE_TIME_FMT.parse(info.getUpdated()));
        dto.info = dtoInfo;
        dto.updatedBy = tenant.updatedBy;
        return dto;
    }

    @IterableMapping(qualifiedByName = "map1")
    List<TenantResponseDto> tenantsToDtos(List<Tenant> tenants);

    @Named("map1")
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



    @Named("map4")
    default TenantRequestDto webApiTenantToTenantRequestDto(TenantWebApiGetResponse.Info info) {
        TenantRequestDto dto = new TenantRequestDto();
        TenantInfoDto dtoInfo = new TenantInfoDto();
        dtoInfo.name = info.getName();
        dtoInfo.email = info.getEmail();
        dtoInfo.website = info.getWebsite();
        dtoInfo.description = info.getDescription();
        dtoInfo.image = info.getImage();
        dtoInfo.createdAt = Instant.from(DATE_TIME_FMT.parse(info.getCreated()));
        dtoInfo.updatedAt = Instant.from(DATE_TIME_FMT.parse(info.getUpdated()));
        dto.info = dtoInfo;
        return dto;
    }

}