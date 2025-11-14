package org.grnet.status.repositories;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.grnet.status.entities.*;

import java.util.Optional;

@ApplicationScoped
public class TenantRepository implements Repository<Tenant, String> {

    public Optional<Tenant> fetchTenantByName(String name) {

        return find("from Tenant t where t.name = ?1", name).stream().findAny();
    }

}