package org.grnet.status.repositories;

import io.quarkus.hibernate.orm.panache.Panache;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.grnet.status.entities.*;

import java.util.Optional;
import java.util.*;

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
    public PageQuery<Tenant> fetchTenantsByPageAndSize(int page, int size, String search,String sort, String order) {
        var joiner = new StringJoiner(StringUtils.SPACE);
        joiner.add("from Tenant t");

        var map = new HashMap<String, Object>();

        if (StringUtils.isNotEmpty(search)) {

            joiner.add("WHERE t.name ilike :search or t.email ilike :search");
            map.put("search", "%" + search + "%");
        }
        if (StringUtils.isNotEmpty(sort)) {

            joiner.add("order by");
            joiner.add("t." + sort);
            joiner.add(order);
        } else {

            joiner.add("order by");
            joiner.add("t.name ASC");
            joiner.add(", t.createdAt DESC");
        }

        var panache = find(joiner.toString(), map).page(page, size);

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