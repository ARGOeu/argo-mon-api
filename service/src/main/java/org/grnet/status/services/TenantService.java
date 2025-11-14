package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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

    /**
     * Delete tenant from the database.
     */
    @Transactional
    public void deleteAll() {

        tenantRepository.deleteAll();
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

}
