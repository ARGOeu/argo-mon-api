package org.grnet.status.repositories;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.grnet.status.entities.*;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class TenantRepository implements Repository<Tenant, String> {

    public Optional<Tenant> fetchTenantByName(String name) {

        return find("from Tenant t where t.name = ?1", name).stream().findAny();
    }


    /**
     * Retrieves a page of tenants from the database.
     *
     * @param page The index of the page to retrieve (starting from 0).
     * @param size The maximum number of users to include in a page.
     * @return A list of Tenant objects representing the users in the
     * requested page.
     */
    public PageQuery<Tenant> fetchTenantsByPageAndSize(int page, int size) {

        var panache = find("from Tenant t", Sort.by("createdAt", Sort.Direction.Descending)).page(page, size);

        var pageable = new PageQueryImpl<Tenant>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }

    /**
     * Retrieves all tenants  from the database.
     *
     * @return A list of Tenant objects representing the users in the
     * requested page.
     */
    public List<Tenant> fetchTenants() {
        return find("from Tenant t", Sort.by("createdAt").descending()).list();
    }

}