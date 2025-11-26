package org.grnet.status.services;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.ws.rs.core.UriInfo;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.tenant.TenantRequestDto;
import org.grnet.status.dtos.tenant.TenantResponseDto;
import org.grnet.status.dtos.tenant.TenantWebApiGetResponse;
import org.grnet.status.entities.Tenant;
import org.grnet.status.exceptions.CustomRuntimeException;
import org.grnet.status.mappers.TenantMapper;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.services.clients.ArgoWebApiClientFactory;
import org.grnet.status.services.utils.EncryptUtil;

import java.util.HashSet;

@ApplicationScoped

public class TenantService {

    @Inject
    TenantRepository tenantRepository;
    @Inject
    EncryptUtil encryptUtil;

    @Inject
    ArgoWebApiClientFactory argoWebApiClientFactory;

    @ConfigProperty(name = "admin.web.api.encrypted.secret")
    String encryptedSecret;
    @ConfigProperty(name = "web.api.url")
    String webapi;

    /**
     * Create a tenant
     *
     * @param request , TenantRequestDto with all the info needed
     * @param userId  , the creator of the tenant
     * @return, TenantResponseDto representing the tenant's info
     */

    @Transactional
    public TenantResponseDto create(TenantRequestDto request, String userId) {

        var decryptedSecret = encryptUtil.decrypt(encryptedSecret);
        var client = argoWebApiClientFactory.buildClient(webapi);

        var tenant = TenantMapper.INSTANCE.dtoToTenant(request.info);
        try {
            var apiResponse = client.createTenant(decryptedSecret, request);   // returns POJO
            tenant.id = apiResponse.getData().getId();
            tenant.updatedBy = userId;
            var existTenant = tenantRepository.findById(tenant.id);
            if (existTenant != null) {
                var message = "Tenant with id: " + tenant.id + " already exists in ARGO Mon Status API";
                throw new CustomRuntimeException(409, message, new HashSet<>());
            }
            tenantRepository.persist(tenant);
            return TenantMapper.INSTANCE.tenantToDto(tenant);

        } catch (WebApplicationException e) {
            int status = e.getResponse().getStatus();
            var message = e.getMessage();
            if (status == 409) {
                var optTenant = tenantRepository.fetchTenantByName(request.info.name);
                if (optTenant.isPresent()) {
                    message = message + ". Existing tenant in Argo Mon Status API has id: " + optTenant.get().id;
                } else {
                    message = message + ". Tenant exists in Argo Web Api but not in Argo Mon Status API";
                }
            }
            throw new WebApplicationException(message, status);

        }
    }


    /**
     * Get a tenant by Id.
     */
    public TenantResponseDto getTenantById(String id) {
        var decryptedSecret = encryptUtil.decrypt(encryptedSecret);
        var client = argoWebApiClientFactory.buildClient(webapi);
        var tenant = tenantRepository.findById(id);
        TenantWebApiGetResponse webApiResponse = null;
        try {
            webApiResponse = client.getTenant(decryptedSecret, id);
            if (tenant != null) {
                matchFields(webApiResponse, tenant);
            }
        } catch (RuntimeException e) {
            int status = 500; // default fallback
            if (e instanceof WebApplicationException) {
                status = ((WebApplicationException) e).getResponse().getStatus();
            }
            var message = e.getMessage();
            throw new WebApplicationException(message, status);
        }
        return TenantMapper.INSTANCE.tenantToDto(tenant);

    }


    private void matchFields(TenantWebApiGetResponse response, Tenant tenant) {
        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            throw new RuntimeException("Response data is missing or empty");
        }

        var info = response.getData().get(0).getInfo();
        if (info == null) {
            throw new RuntimeException("Tenant info is missing in the response");
        }

        checkField("Name", info.getName(), tenant.name);
        checkField("Email", info.getEmail(), tenant.email);
        checkField("Description", info.getDescription(), tenant.description);
        checkField("Image", info.getImage(), tenant.image);
        checkField("Website", info.getWebsite(), tenant.website);
    }

    private void checkField(String fieldName, Object webApiValue, Object tenantValue) {
        if (webApiValue == null && tenantValue == null) {
            return; // Both null, considered equal
        }
        if (webApiValue == null || tenantValue == null || !webApiValue.equals(tenantValue)) {
            throw new RuntimeException(fieldName + " does not match for tenant between ARGO Web API and Argo Mon Status");
        }
    }

    /**
     * Delete a tenant by Id.
     */
    @Transactional
    public void deleteTenantById(String id) {
        var decryptedSecret = encryptUtil.decrypt(encryptedSecret);
        var client = argoWebApiClientFactory.buildClient(webapi);
        var tenant = tenantRepository.findById(id);
        try {
            client.deleteTenant(id, decryptedSecret);
            tenantRepository.delete(tenant);
        } catch (RuntimeException e) {
            int status = 500; // default fallback
            if (e instanceof WebApplicationException) {
                status = ((WebApplicationException) e).getResponse().getStatus();
            }
            var message = e.getMessage();
            throw new WebApplicationException(message, status);
        }
    }

    /**
     * * Delete all tenants.
     */
    @Transactional
    public void deleteAll() {

        var tenants = tenantRepository.fetchTenants();

        tenants.forEach(t -> {
            var decryptedSecret = encryptUtil.decrypt(encryptedSecret);
            var client = argoWebApiClientFactory.buildClient(webapi);

            try {
                client.deleteTenant(t.id, decryptedSecret);

            } catch (RuntimeException e) {
                int status = 500;
                if (e instanceof WebApplicationException) {
                    status = ((WebApplicationException) e).getResponse().getStatus();
                }
                var message = e.getMessage();
                Log.info("INFO -- STATUS : " + status + " " + message);
                //  throw new WebApplicationException(message, status);
            }
            tenantRepository.delete(t);

        });
    }

    /**
     * Update an existing tenant.
     */
    @Transactional
    public TenantResponseDto updateTenant(String id, TenantRequestDto request) {

        var decryptedSecret = encryptUtil.decrypt(encryptedSecret);
        var client = argoWebApiClientFactory.buildClient(webapi);
        var tenant = tenantRepository.findById(id);
        try {
            client.updateTenant(id, decryptedSecret, request);
            TenantMapper.INSTANCE.updateToTenant(request, tenant);
            tenantRepository.persist(tenant);

        } catch (RuntimeException e) {
            int status = 500; // default fallback
            if (e instanceof WebApplicationException) {
                status = ((WebApplicationException) e).getResponse().getStatus();
            }
            var message = e.getMessage();
            throw new WebApplicationException(message, status);
        }
        return TenantMapper.INSTANCE.tenantToDto(tenant);
    }

    /**
     * Retrieves a page of tenant objects existing.
     *
     * @param page    The index of the page to retrieve (starting from 0).
     * @param size    The maximum number of assessment objects to include in a page.
     * @param uriInfo The Uri Info.
     * @return A list of TemplateSubjectDto objects representing the submitted assessment objects in the requested page.
     */
    public PageResource<TenantResponseDto> getTenantsByPageAndSize(int page, int size, UriInfo uriInfo, String
            tenantName, String tenantEmail) {

        var tenants = tenantRepository.fetchTenantsByPageAndSize(page, size, tenantName, tenantEmail);
        return new PageResource<>(tenants, TenantMapper.INSTANCE.tenantsToDtos(tenants.list()), uriInfo);

    }


}
