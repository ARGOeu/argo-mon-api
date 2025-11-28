package org.grnet.status.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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
import org.grnet.status.services.utils.ImageUploadUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
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

    @ConfigProperty(name = "base.upload.logo.dir")
    String baseUploadTenantsImagesDir;

    @Inject
    ImageUploadUtil imageUploadUtil;
    @ConfigProperty(name = "api.server.url")
    String apiServerUrl;


    @Inject
    ObjectMapper objectMapper;

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
        String image = null;
        try {
            image = handleImage(request);
            if (image != null) {
                request.info.image = image;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        //   var webapiRequest = TenantMapper.INSTANCE.requestToWebApiRequest(request,image);

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

        } catch (RuntimeException e) {
            int status = 500; // default fallback
            if (e instanceof WebApplicationException) {
                status = ((WebApplicationException) e).getResponse().getStatus();
            }
            var message = e.getMessage();
            throw new WebApplicationException(message, status);
        }
        return TenantMapper.INSTANCE.webApiTenantToDto(tenant, webApiResponse.getData().get(0).getInfo());

    }


    private void matchFields(TenantWebApiGetResponse response, Tenant tenant) {
        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            throw new RuntimeException("Response data is missing or empty");
        }

        var info = response.getData().get(0).getInfo();
        if (info == null) {
            throw new RuntimeException("Tenant info is missing in the response");
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
            imageUploadUtil.deleteImageIfExists(baseUploadTenantsImagesDir,tenant.name);
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
        String image = null;
        try {
            image = handleImage(request);
            if (image != null) {
                request.info.image = image;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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


    private String handleImage(TenantRequestDto request) throws IOException {

        var image = request.info.image;
        if (image != null && image.startsWith("data:image/")) {
            imageUploadUtil.validateBase64Image(image);
            var savedPath = imageUploadUtil.saveBase64Image(baseUploadTenantsImagesDir, image, request.info.name, "/logos/");

            return apiServerUrl + savedPath;
        }
        return null;
    }
}
