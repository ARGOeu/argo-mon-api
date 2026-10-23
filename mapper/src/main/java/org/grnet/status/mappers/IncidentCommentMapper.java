package org.grnet.status.mappers;

import org.grnet.status.dtos.incident.IncidentCommentResponseDto;
import org.grnet.status.entities.IncidentComment;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface IncidentCommentMapper {

    IncidentCommentMapper INSTANCE =
            Mappers.getMapper(IncidentCommentMapper.class);

    @IterableMapping(qualifiedByName = "incidentCommentMap")
    List<IncidentCommentResponseDto> incidentCommentsToDtos(List<IncidentComment> incidentComments);

    @Named("incidentCommentMap")
    IncidentCommentResponseDto incidentCommentToDto(IncidentComment incidentComment);
}