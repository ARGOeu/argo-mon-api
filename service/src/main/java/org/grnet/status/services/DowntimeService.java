package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.UriInfo;
import org.grnet.status.dtos.downtime.DowntimeRequest;
import org.grnet.status.dtos.downtime.DowntimeResponse;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.entities.Downtime;
import org.grnet.status.entities.DowntimeServiceEndpoint;
import org.grnet.status.enums.DowntimeClassification;
import org.grnet.status.mappers.DowntimeMapper;
import org.grnet.status.repositories.DowntimeRepository;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.util.Utility;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class DowntimeService {

    @Inject
    DowntimeRepository downtimeRepository;

    @Inject
    TenantRepository tenantRepository;

    @Inject
    Utility utility;


    /**
     * Creates a new downtime entry for a specific tenant.
     * <p>
     * The downtime is stored only in the local database and is not propagated
     * to external systems.
     * <p>
     * The downtime classification is calculated based on the scheduled time:
     * - Scheduled: downtime starts at least 24 hours after creation time.
     * - Unscheduled: downtime starts within the next 24 hours.
     * <p>
     * All timestamps are stored as UTC instants with second precision.
     */
    @Transactional
    public DowntimeResponse addDowntime(String id, DowntimeRequest request) {

        // Use UTC timestamp truncated to seconds to keep consistent API responses.
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        var tenant = tenantRepository.findById(id);
        Downtime downtime = DowntimeMapper.INSTANCE.dtoToDowntime(request);

        downtime.setTenant(tenant.id);
        downtime.setCreatedBy(utility.getUid());
        downtime.setCreatedAt(now);


//          Determine whether the downtime is scheduled or unscheduled.
//
//          A downtime is considered scheduled only when it is planned
//          at least 24 hours before its start time.
//

        if (request.getScheduledAt() != null) {

            Instant scheduledAt = request.getScheduledAt();

            if (!scheduledAt.isBefore(now.plus(24, ChronoUnit.HOURS))) {
                downtime.setClassification(DowntimeClassification.Scheduled.name());
            } else {
                downtime.setClassification(DowntimeClassification.Unscheduled.name());
            }
        }


//          Map the associated service endpoints.
//
//          The relationship is bidirectional, therefore each endpoint must
//          reference the parent downtime entity before persisting.

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

    /**
     * Retrieves downtimes for a tenant using pagination.
     * <p>
     * If a date is provided, only downtimes active during that UTC day are returned.
     * The input date format is dd-MM-yyyy.
     */
    @Transactional
    public PageResource<DowntimeResponse> fetchDowntimesByPageAndSize(
            int page,
            int size,
            String id,
            String date,
            UriInfo uriInfo) {

        Instant[] timestamps = new Instant[2];

        /*
         * Date filtering is optional.
         * When provided, it is converted into a UTC start/end range.
         */
        if (date != null && !date.isBlank()) {
            timestamps = convertDate(date);
        }

        var tenant = tenantRepository.findById(id);

        var downtimes = downtimeRepository.findByTenantPageAndSize(
                page,
                size,
                tenant.id,
                timestamps[0],
                timestamps[1]
        );

        return new PageResource<>(
                downtimes,
                DowntimeMapper.INSTANCE.downtimesToDtos(downtimes.list()),
                uriInfo
        );
    }

    @Transactional
    public DowntimeResponse fetchDowntimes(String id, String downtimeId) {

        var tenant = tenantRepository.findById(id);

        var downtime = downtimeRepository.findById(downtimeId);
        if (!downtime.getTenant().equals(id)) {
            throw new ForbiddenException(
                    String.format("Downtime with id %s cannot be accessed for tenant %s", downtimeId, id)
            );
        }
        return  DowntimeMapper.INSTANCE.downtimeToDto(downtime);
    }


    /**
     * Converts a date provided by the API (dd-MM-yyyy) into a UTC time range.
     * <p>
     * Example:
     * Input: 08-07-2027
     * <p>
     * Output:
     * Start: 2027-07-08T00:00:00Z
     * End: 2027-07-08T23:59:59Z
     * <p>
     * This ensures that filtering is independent of the server timezone.
     */
    private Instant[] convertDate(String date) {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate localDate = LocalDate.parse(date, formatter);

        Instant startDate = localDate
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        Instant endDate = localDate
                .atTime(23, 59, 59)
                .toInstant(ZoneOffset.UTC);

        return new Instant[]{startDate, endDate};
    }
    @Transactional
    public void deleteDowntime(String id, String downtimeId) {

        var tenant = tenantRepository.findById(id);

        var downtime = downtimeRepository.findById(downtimeId);

        if (!downtime.getTenant().equals(id)) {
            throw new ForbiddenException(
                    String.format("Downtime with id %s cannot be deleted for tenant %s", downtimeId, id)
            );
        }

        downtimeRepository.delete(downtime);
    }
    @Transactional
    public DowntimeResponse updateDowntime(String tenantId,String downtimeId,DowntimeRequest request) {

        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        tenantRepository.findById(tenantId);

        Downtime downtime = downtimeRepository.findById(downtimeId);

        if (!downtime.getTenant().equals(tenantId)) {
            throw new ForbiddenException(
                    String.format(
                            "Downtime with id %s cannot be updated for tenant %s",
                            downtimeId,
                            tenantId
                    )
            );
        }

        // Map request fields to existing entity
        DowntimeMapper.INSTANCE.updateDowntime(request, downtime);


        // Recalculate classification
        if (!request.getScheduledAt().isBefore(now.plus(24, ChronoUnit.HOURS))) {
            downtime.setClassification(DowntimeClassification.Scheduled.name());
        } else {
            downtime.setClassification(DowntimeClassification.Unscheduled.name());
        }


        // Update audit fields
        downtime.setUpdatedAt(now);
        downtime.setUpdatedBy(utility.getUid());


        // Replace services
        downtime.getServices().clear();

        if (request.getServices() != null) {
            request.getServices().forEach(serviceRequest -> {

                DowntimeServiceEndpoint service =
                        DowntimeMapper.INSTANCE.dtoToDowntimeService(serviceRequest);

                service.setDowntime(downtime);

                downtime.getServices().add(service);
            });
        }
        return DowntimeMapper.INSTANCE.downtimeToDto(downtime);
    }
}