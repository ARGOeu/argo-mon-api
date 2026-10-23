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

    /**
     * Retrieves an incident activity by its identifier and incident identifier.
     *
     * @param activityId incident activity identifier
     * @param incidentId incident identifier
     * @return incident activity
     */
    public IncidentActivity fetchByIdAndIncidentId(String activityId, String incidentId) {

        return find("id = ?1 and incident.id = ?2", activityId, incidentId).firstResult();
    }
}