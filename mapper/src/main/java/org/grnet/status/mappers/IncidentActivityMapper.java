package org.grnet.status.mappers;

import org.grnet.status.dtos.incident.IncidentActivityResponseDto;
import org.grnet.status.entities.IncidentActivity;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface IncidentActivityMapper {

    IncidentActivityMapper INSTANCE = Mappers.getMapper(IncidentActivityMapper.class);

    @Named("incidentActivityMap")
    IncidentActivityResponseDto incidentActivityToDto(IncidentActivity activity);

    @Named("incidentActivityMap")
    List<IncidentActivityResponseDto> incidentActivitiesToDtos(List<IncidentActivity> activities);
}