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
    public PageQuery<Tenant> fetchTenantsByPageAndSize(int page, int size, String tenantName, String tenantEmail) {

        StringJoiner joiner = new StringJoiner(" ");
        joiner.add("select t.* from t_tenant t where 1=1");

        StringJoiner counter = new StringJoiner(" ");
        counter.add("select count(t.id) from t_tenant t where 1=1");

        Map<String, Object> params = new HashMap<>();

        if (StringUtils.isNotEmpty(tenantName)) {
            joiner.add("and t.name = :name");
            counter.add("and t.name = :name");
            params.put("name", tenantName);
        }

        if (StringUtils.isNotEmpty(tenantEmail)) {
            joiner.add("and t.email = :email");
            counter.add("and t.email = :email");
            params.put("email", tenantEmail);
        }

        joiner.add("order by t.created_at desc");

        var em = Panache.getEntityManager();

        var query = em.createNativeQuery(joiner.toString(), Tenant.class);
        var countQuery = em.createNativeQuery(counter.toString());

        params.forEach((key, value) -> {
            query.setParameter(key, value);
            countQuery.setParameter(key, value);
        });

        List<Tenant> list = query
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        PageQueryImpl<Tenant> pageable = new PageQueryImpl<>();
        pageable.list = list;
        pageable.index = page;
        pageable.size = size;
        pageable.count = ((Number) countQuery.getSingleResult()).longValue();
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