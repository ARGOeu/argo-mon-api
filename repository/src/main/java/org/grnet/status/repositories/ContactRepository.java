package org.grnet.status.repositories;


import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.grnet.status.entities.*;
import org.grnet.status.enums.ContactType;

import java.util.HashMap;
import java.util.Optional;
import java.util.StringJoiner;

@ApplicationScoped
public class ContactRepository implements Repository<Contact, String> {

    public Optional<Contact> fetchContactByName(String name) {
        return find("contactName", name).firstResultOptional();
    }

    public Optional<Contact> fetchContactByInfo(String name, String email, String type) {
        return find("from Contact c where c.contactName = :name AND c.contactEmail = :email AND c.contactType = :type",
                Parameters.with("name", name).and("email", email).and("type", ContactType.valueOf(type)))
                .stream().findAny();
    }
    /**
     * Retrieves a page of tenants from the database.
     *
     * @param page The index of the page to retrieve (starting from 0).
     * @param size The maximum number of users to include in a page.
     * @return A list of Tenant objects representing the users in the
     * requested page.
     */
    public PageQuery<Contact> fetchContactsByPageAndSize(int page, int size) {
        var panache = find("from Contact c ").page(page, size);

        var pageable = new PageQueryImpl<Contact>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;

    }

}