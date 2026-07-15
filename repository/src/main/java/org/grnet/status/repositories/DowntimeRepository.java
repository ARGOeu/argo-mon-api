package org.grnet.status.repositories;

import jakarta.enterprise.context.ApplicationScoped;
import org.grnet.status.entities.Downtime;

@ApplicationScoped
public class DowntimeRepository implements Repository<Downtime,String> {

}
