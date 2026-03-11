package org.grnet.status.repositories;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.grnet.endpoint.scanner.runtime.RepositoryRegistrar;
import org.grnet.endpoint.scanner.runtime.resolvers.RepositoryRegistry;

@ApplicationScoped
public class ProjectARepositoryRegistrar implements RepositoryRegistrar {
    @Inject
    TenantRepository tenantRepository;

    @Override
    public void registerRepositories(RepositoryRegistry registry) {
        registry.register("Tenant", tenantRepository);
    }
}
