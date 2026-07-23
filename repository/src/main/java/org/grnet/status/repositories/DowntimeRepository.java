package org.grnet.status.repositories;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.grnet.status.entities.Downtime;
import org.grnet.status.entities.Page;
import org.grnet.status.entities.PageQuery;
import org.grnet.status.entities.PageQueryImpl;
import org.grnet.status.entities.*;
import org.grnet.status.entities.*;
import org.grnet.status.entities.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;

@ApplicationScoped
public class DowntimeRepository implements Repository<Downtime,String> {
    public PageQuery<Downtime> findByTenantPageAndSize(
            int page,
            int size,
            String tenantId,
            Instant startDate,
            Instant endDate) {

        var joiner = new StringJoiner(StringUtils.SPACE);
        joiner.add("from Downtime d");
        joiner.add("where d.tenant = :tenantId");

        var map = new HashMap<String, Object>();
        map.put("tenantId", tenantId);

        if (startDate != null && endDate != null) {
            joiner.add("and d.scheduledAt <= :endDate");
            joiner.add("and d.completedAt >= :startDate");

            map.put("startDate", startDate);
            map.put("endDate", endDate);
        }

        joiner.add("order by d.scheduledAt DESC");

        var panache = find(joiner.toString(), map).page(page , size);

        var pageable = new PageQueryImpl<Downtime>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page , size);

        return pageable;
    }
    public PageQuery<Downtime> findB(
            int page,
            int size,
            String tenantId,
            Instant startDate,
            Instant endDate) {

        var joiner = new StringJoiner(StringUtils.SPACE);
        joiner.add("from Downtime d");
        joiner.add("where d.tenant = :tenantId");

        var map = new HashMap<String, Object>();
        map.put("tenantId", tenantId);

        if (startDate != null && endDate != null) {
            joiner.add("and d.scheduledAt <= :endDate");
            joiner.add("and d.completedAt >= :startDate");

            map.put("startDate", startDate);
            map.put("endDate", endDate);
        }

        joiner.add("order by d.scheduledAt DESC");

        var panache = find(joiner.toString(), map).page(page , size);

        var pageable = new PageQueryImpl<Downtime>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page , size);

        return pageable;
    }

    public List<Downtime> findOverlappingDowntimes(
            String tenantId,
            Instant startDate,
            Instant endDate) {

        var joiner = new StringJoiner(StringUtils.SPACE);

        joiner.add("from Downtime d");
        joiner.add("where d.tenant = :tenantId");
        joiner.add("and d.scheduledAt <= :endDate");
        joiner.add("and (d.completedAt is null or d.completedAt >= :startDate)");
        joiner.add("order by d.scheduledAt DESC");

        var map = new HashMap<String, Object>();
        map.put("tenantId", tenantId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);

        return find(joiner.toString(), map).list();
    }
    public boolean existsOverlappingDowntime(
            String tenantId,
            Instant scheduledAt,
            Instant completedAt) {

        return count("""
            tenant = ?1
            AND scheduledAt <= ?3
            AND (completedAt IS NULL OR completedAt >= ?2)
            """,
                tenantId,
                scheduledAt,
                completedAt == null ? Instant.MAX : completedAt
        ) > 0;
    }
}
