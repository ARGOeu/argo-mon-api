package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriInfo;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.tenant.ContactDto;
import org.grnet.status.dtos.tenant.TenantResponseDto;
import org.grnet.status.entities.Contact;
import org.grnet.status.mappers.StatusPageMapper;
import org.grnet.status.mappers.TenantMapper;
import org.grnet.status.repositories.ContactRepository;

import java.util.ArrayList;
import java.util.HashSet;

@ApplicationScoped

public class ContactService {

    @Inject
    ContactRepository contactRepository;



    /**
     * Retrieves a page of contact objects existing.
     *
     * @param page    The index of the page to retrieve (starting from 0).
     * @param size    The maximum number of contact objects to include in a page.
     * @param uriInfo The Uri Info.
     * @return A list of ContactDto objects representing the submitted contact objects in the requested page.
     */
    public PageResource<ContactDto> getContactsByPageAndSize(int page, int size, UriInfo uriInfo) {

        var contacts = contactRepository.fetchContactsByPageAndSize(page, size);
        return new PageResource<>(contacts,TenantMapper.INSTANCE.contactsToDtos(new HashSet<>(contacts.list())), uriInfo);
    }



}