package org.grnet.status.repositories;

import jakarta.enterprise.context.ApplicationScoped;
import org.grnet.status.entities.IncidentActivity;

import java.util.List;

/**
 * Repository responsible for managing IncidentActivity entities.
 */
@ApplicationScoped
public class IncidentActivityRepository implements Repository<IncidentActivity, String> {

    /**
     * Retrieves the activity history of an incident.
     *
     * @param incidentId incident identifier
     * @return incident activity history
     */
    public List<IncidentActivity> fetchByIncidentId(String incidentId) {

        return find("incident.id = ?1 order by createdAt asc", incidentId).list();
    }

    public IncidentActivity findLatestByIncidentId(String incidentId) {

        return find("incident.id = ?1 order by createdAt desc", incidentId).firstResult();
    }
}