package org.grnet.status.repositories;

import jakarta.enterprise.context.ApplicationScoped;
import org.grnet.status.entities.IncidentComment;

/**
 * Repository responsible for managing IncidentComment entities.
 */
@ApplicationScoped
public class IncidentCommentRepository implements Repository<IncidentComment, String> {
}