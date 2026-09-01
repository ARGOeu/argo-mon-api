package org.grnet.status.mappers;

import org.grnet.status.dtos.incident.IncidentRequestDto;
import org.grnet.status.dtos.incident.IncidentResponseDto;
import org.grnet.status.dtos.incident.ServiceDto;
import org.grnet.status.entities.Incident;
import org.grnet.status.entities.IncidentAffectedService;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Mapper(imports = {Timestamp.class, Instant.class})
public interface IncidentMapper {

    IncidentMapper INSTANCE = Mappers.getMapper(IncidentMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "incidentNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "statusDescription", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "comments", ignore = true)
    Incident incidentRequestToEntity(IncidentRequestDto request);

    @Mapping(source = "id", target = "serviceId")
    @Mapping(source = "name", target = "serviceName")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "incident", ignore = true)
    IncidentAffectedService serviceGroupDtoToEntity(ServiceDto service);

    @IterableMapping(qualifiedByName = "incidentMap")
    List<IncidentResponseDto> incidentsToDtos(List<Incident> incidents);

    @Named("incidentMap")
    IncidentResponseDto incidentToResponseDto(Incident incident);

    @Mapping(source = "serviceId", target = "id")
    @Mapping(source = "serviceName", target = "name")
    ServiceDto incidentServiceGroupToDto(IncidentAffectedService service);
}
