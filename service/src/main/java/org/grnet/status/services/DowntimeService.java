package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.grnet.status.dtos.downtime.DowntimeRequest;
import org.grnet.status.dtos.downtime.DowntimeResponse;
import org.grnet.status.entities.Downtime;
import org.grnet.status.entities.DowntimeServiceEndpoint;
import org.grnet.status.enums.DowntimeClassification;
import org.grnet.status.mappers.DowntimeMapper;
import org.grnet.status.repositories.DowntimeRepository;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.util.Utility;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class DowntimeService {

    @Inject
    DowntimeRepository downtimeRepository;
    @Inject
    TenantRepository tenantRepository;
    @Inject
    Utility utility;

    @Transactional
    public DowntimeResponse addDowntime(String id, DowntimeRequest request) {

        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        var tenant=tenantRepository.findById(id);
        Downtime downtime = DowntimeMapper.INSTANCE.dtoToDowntime(request);
        downtime.setTenant(tenant.id);
        downtime.setCreatedBy(utility.getUid());
        downtime.setCreatedAt(now);
        if (request.getScheduledAt() != null) {

            Instant scheduledAt = request.getScheduledAt();

            if (!scheduledAt.isBefore(now.plus(24, ChronoUnit.HOURS))) {
                downtime.setClassification(DowntimeClassification.Scheduled.name());
            } else {
                downtime.setClassification(DowntimeClassification.Unscheduled.name());
            }

        }

        if (request.getServices() != null) {
            request.getServices().forEach(serviceRequest -> {
                DowntimeServiceEndpoint serviceEndpoint =
                        DowntimeMapper.INSTANCE.dtoToDowntimeService(serviceRequest);

                serviceEndpoint.setDowntime(downtime);
                downtime.getServices().add(serviceEndpoint);
            });
        }

        downtimeRepository.persist(downtime);

        return DowntimeMapper.INSTANCE.downtimeToDto(downtime);
    }
}