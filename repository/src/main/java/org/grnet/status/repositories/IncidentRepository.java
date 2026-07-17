package org.grnet.status.repositories;

import jakarta.enterprise.context.ApplicationScoped;
import org.grnet.status.entities.Incident;

import java.util.Optional;

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
    public Optional<Incident> fetchByIdAndTenantId(String id, String tenantId) {
        return find(
                "id = ?1 and tenant.id = ?2",
                id,
                tenantId
        ).firstResultOptional();
    }
}