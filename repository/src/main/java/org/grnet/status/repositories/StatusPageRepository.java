package org.grnet.status.repositories;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.grnet.status.entities.Page;
import org.grnet.status.entities.PageQuery;
import org.grnet.status.entities.PageQueryImpl;
import org.grnet.status.entities.StatusPage;


@ApplicationScoped
public class StatusPageRepository implements Repository<StatusPage, String> {

    public PageQuery<StatusPage> fetchStatusPageByUserAndPage(int page, int size, String userID){

        var panache = find("from StatusPage sp where sp.userId = ?1", Sort.by("createdAt", Sort.Direction.Descending), userID).page(page, size);

        var pageable = new PageQueryImpl<StatusPage>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }

    public PageQuery<StatusPage> fetchStatusPageByPage(int page, int size){

        var panache = find("from StatusPage sp", Sort.by("createdAt", Sort.Direction.Descending)).page(page, size);

        var pageable = new PageQueryImpl<StatusPage>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }
}
