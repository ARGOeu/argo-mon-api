package org.grnet.status.repositories;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.grnet.status.entities.*;


@ApplicationScoped
public class ProjectRepository implements Repository<Project, String> {

    public PageQuery<Project> fetchProjectByPage(int page, int size){

        var panache = find("from Project p", Sort.by("createdAt", Sort.Direction.Descending)).page(page, size);

        var pageable = new PageQueryImpl<Project>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }
}
