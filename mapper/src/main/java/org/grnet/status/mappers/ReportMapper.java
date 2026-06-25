package org.grnet.status.mappers;

import org.apache.commons.lang3.StringUtils;
import org.grnet.status.dtos.report.FullReportResponseDto;
import org.grnet.status.dtos.report.MiniReportResponse;
import org.grnet.status.dtos.report.PartialReportResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.sql.Timestamp;
import java.time.Instant;

@Mapper(imports = {StringUtils.class, Timestamp.class, Instant.class})
public interface ReportMapper {

    ReportMapper INSTANCE = Mappers.getMapper(ReportMapper.class);

    @Mapping(target = "tenantName", source = "tenant")
    @Mapping(target = "name", source = "info.name")
    @Mapping(target = "description", source = "info.description")
    @Mapping(target = "createdAt", source = "info.created")
    @Mapping(target = "updatedAt", source = "info.updated")
    @Mapping(target = "nodeReport", source = "nodeReport")
    @Mapping(target = "publicReport", source = "publicReport", defaultValue = "false")
    PartialReportResponseDto fullToPartialReport(FullReportResponseDto source);


    @Mapping(target = "name", source = "info.name")
    @Mapping(target = "node", source = "nodeReport")
    MiniReportResponse fullToMiniReport(FullReportResponseDto source);

    default Instant map(Timestamp timestamp) {
        return timestamp != null ? timestamp.toInstant() : null;
    }

    default Timestamp map(Instant instant) {
        return instant != null ? Timestamp.from(instant) : null;
    }
}
