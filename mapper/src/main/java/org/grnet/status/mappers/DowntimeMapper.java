package org.grnet.status.mappers;

import org.grnet.status.dtos.downtime.DowntimeRequest;
import org.grnet.status.dtos.downtime.DowntimeResponse;
import org.grnet.status.dtos.downtime.DowntimeServiceEndpointRequest;
import org.grnet.status.entities.Downtime;
import org.grnet.status.entities.DowntimeServiceEndpoint;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
@Mapper(imports = {Timestamp.class, Instant.class})
public interface DowntimeMapper {

    DowntimeMapper INSTANCE = Mappers.getMapper(DowntimeMapper.class);

    @Mapping(target = "services", ignore = true)
    Downtime dtoToDowntime(DowntimeRequest dto);

    DowntimeServiceEndpoint dtoToDowntimeService(DowntimeServiceEndpointRequest dto);

    DowntimeResponse downtimeToDto(Downtime entity);

    List<DowntimeResponse> downtimesToDtos(List<Downtime> entities);

    @Mapping(target = "id", ignore = true)
    void updateDowntime(DowntimeRequest dto, @MappingTarget Downtime entity);


    default Timestamp map(Instant instant) {
        return instant != null ? Timestamp.from(instant) : null;
    }

    default Timestamp map(LocalDateTime localDateTime) {
        return localDateTime != null ? Timestamp.valueOf(localDateTime) : null;
    }
    default Instant map(Timestamp timestamp) {
        return timestamp != null
                ? timestamp.toInstant().truncatedTo(ChronoUnit.SECONDS)
                : null;
    }
}