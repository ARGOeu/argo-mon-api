package org.grnet.status.repositories;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.grnet.status.entities.Incident;
import org.grnet.status.entities.Page;
import org.grnet.status.entities.PageQuery;
import org.grnet.status.entities.PageQueryImpl;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Repository responsible for managing Incident entities.
 */
@ApplicationScoped
public class IncidentRepository implements Repository<Incident, String> {

    /**
     * Retrieves the next value from the incident number sequence.
     *
     * @return next incident sequence value
     */
    public long nextIncidentSequenceValue() {
        return ((Number) getEntityManager()
                .createNativeQuery("SELECT nextval('incident_number_seq')")
                .getSingleResult())
                .longValue();
    }

    /**
     * Retrieves an incident by its human-readable incident number.
     *
     * @param incidentNumber incident number
     * @return optional incident
     */
    public Optional<Incident> fetchByIncidentNumber(String incidentNumber) {
        return find("incidentNumber", incidentNumber).firstResultOptional();
    }

    /**
     * Retrieves an incident by its identifier and tenant identifier.
     *
     * @param id incident identifier
     * @param tenantId tenant identifier
     * @return optional incident
     */
    public Incident fetchByIdAndTenantId(String id, String tenantId) {
        return find(
                "id = ?1 and tenant.id = ?2",
                id,
                tenantId
        ).firstResult();
    }

    /**
     * Retrieves a paginated list of incidents belonging to a tenant.
     *
     * @param tenantId tenant identifier
     * @param page 0-based page index
     * @param size page size
     * @param search optional search term applied to incident title and service name
     * @return paginated incidents
     */
    public PageQuery<Incident> fetchIncidentsByTenantIdByPageAndSize(String tenantId, int page, int size, String search, String date) {

        var joiner = new StringJoiner(StringUtils.SPACE);

        joiner.add("select distinct i");
        joiner.add("from Incident i");
        joiner.add("left join i.services s");
        joiner.add("where i.tenant.id = :tenantId");

        var map = new HashMap<String, Object>();
        map.put("tenantId", tenantId);

        if (StringUtils.isNotBlank(search)) {

            joiner.add("and (i.title ilike :search ");
            joiner.add("or s.serviceName ilike :search ");
            joiner.add("or i.incidentNumber ilike :search)");

            map.put("search", "%" + search.trim() + "%");
        }

        if (StringUtils.isNotBlank(date)) {
            var requestedDate = LocalDate.parse(date);

            var startOfDay = requestedDate.atStartOfDay(ZoneOffset.UTC).toInstant();
            var startOfNextDay = requestedDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

            joiner.add("and i.createdAt >= :startOfDay and i.createdAt < :startOfNextDay");
            map.put("startOfDay", startOfDay);
            map.put("startOfNextDay", startOfNextDay);
        }

        joiner.add("order by i.createdAt desc");

        var panache = find(joiner.toString(), map).page(page, size);

        var pageable = new PageQueryImpl<Incident>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }
}