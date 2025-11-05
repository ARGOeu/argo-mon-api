package org.grnet.status.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.grnet.status.dtos.statuspage.StatusPageConfigDto;
import org.grnet.status.dtos.statuspage.StatusPageRequestDto;
import org.grnet.status.dtos.statuspage.StatusPageUpdateRequestDto;
import org.grnet.status.dtos.statuspage.StatusPageResponseDto;
import org.grnet.status.entities.StatusPage;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Mapper(imports = {Timestamp.class, Instant.class})
public interface StatusPageMapper {

    StatusPageMapper INSTANCE = Mappers.getMapper(StatusPageMapper.class);

    @IterableMapping(qualifiedByName = "map")
    List<StatusPageResponseDto> entitiesToDtos(List<StatusPage> entities);

    @Named("map")
    StatusPageResponseDto entityToDto(StatusPage entity);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", expression = "java(Timestamp.from(Instant.now()))")
    @Mapping(target = "updatedAt", expression = "java(Timestamp.from(Instant.now()))")
    StatusPage dtoToEntity(StatusPageRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(Timestamp.from(Instant.now()))")
    void updateToEntity(StatusPageUpdateRequestDto dto, @MappingTarget StatusPage entity);


    // Helper methods for Timestamp <-> Instant mapping
    default Instant map(Timestamp timestamp) {
        return timestamp != null ? timestamp.toInstant() : null;
    }

    default Timestamp map(Instant instant) {
        return instant != null ? Timestamp.from(instant) : null;
    }

    ObjectMapper MAPPER = new ObjectMapper();

    @SneakyThrows
    default StatusPageConfigDto map(String json) {
        if (json == null) return null;
        return new ObjectMapper().readValue(json, StatusPageConfigDto.class);
    }

    @SneakyThrows
    default String map(StatusPageConfigDto dto) {
        if (dto == null) return null;
        return new ObjectMapper().writeValueAsString(dto);
    }

}
