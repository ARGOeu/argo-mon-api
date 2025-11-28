package org.grnet.status.services;

import com.fasterxml.jackson.databind.ObjectMapper;
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


        var existTenantOpt = tenantRepository.fetchTenantByName(request.info.name);
        if (existTenantOpt.isPresent()) {
            var message = "Tenant with id: " + existTenantOpt.get().id + " already exists in ARGO Mon Status API";
            throw new CustomRuntimeException(409, message, new HashSet<>());
        }

        var decryptedSecret = encryptUtil.decrypt(encryptedSecret);
        var client = argoWebApiClientFactory.buildClient(webapi);
        try {
            var image = request.info.image;
            if (image != null && image.startsWith("data:image/")) {
                var imageUrl = handleImage(request);
                request.info.image = imageUrl;
            }
            // If not Base64, leave image as-is (null or external URL)

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var tenant = TenantMapper.INSTANCE.dtoToTenant(request.info);
        boolean tenantCreatedRemotely = false;
        String remoteTenantId = null;

        try {
            var apiResponse = client.createTenant(decryptedSecret, request);
            remoteTenantId = apiResponse.getData().getId();
            tenantCreatedRemotely = true;

            tenant.id = remoteTenantId;
            tenant.updatedBy = userId;
            tenantRepository.persist(tenant);

            return TenantMapper.INSTANCE.tenantToDto(tenant);

        } catch (Exception e) {
            // If tenant was created remotely, but something failed locally, rollback remote creation
            if (tenantCreatedRemotely && remoteTenantId != null) {
                try {
                    client.deleteTenant( remoteTenantId,decryptedSecret); // Make sure you have this method in your client
                } catch (Exception rollbackEx) {
                    // Log rollback failure, but do not mask original exception
                    System.err.println("Rollback failed for tenant id " + remoteTenantId + ": " + rollbackEx.getMessage());
                }
            }

            if (e instanceof WebApplicationException) {
                WebApplicationException wae = (WebApplicationException) e;
                int status = wae.getResponse().getStatus();
                var message = wae.getMessage();
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

            // rethrow original exception
            throw e;
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
            // First delete from DB (within transaction)
            tenantRepository.delete(tenant);
            imageUploadUtil.deleteImageIfExists(baseUploadTenantsImagesDir, tenant.name);

            // Only after DB deletion succeeded, delete from external API
            client.deleteTenant(id, decryptedSecret);

        } catch (RuntimeException e) {
            int status = 500; // fallback

            if (e instanceof WebApplicationException) {
                status = ((WebApplicationException) e).getResponse().getStatus();
            }

            throw new WebApplicationException(e.getMessage(), status);
        }
    }
    /**
     * Delete all tenants.
     */
    @Transactional
    public void deleteAll() {

        var tenants = tenantRepository.fetchTenants();

        tenants.forEach(t -> {
            try {
                // 1. Delete from DB first (inside transaction)
                tenantRepository.delete(t);
                imageUploadUtil.deleteImageIfExists(baseUploadTenantsImagesDir, t.name);

                // 2. Only after DB delete succeeds, call external API
                var decryptedSecret = encryptUtil.decrypt(encryptedSecret);
                var client = argoWebApiClientFactory.buildClient(webapi);
                client.deleteTenant(t.id, decryptedSecret);

            } catch (RuntimeException e) {

                // If DB delete fails -> API delete is NOT executed, as desired

                int status = 500;
                if (e instanceof WebApplicationException) {
                    status = ((WebApplicationException) e).getResponse().getStatus();
                }

                var message = e.getMessage();
                Log.error("ERROR deleting tenant " + t.id + " -> " + status + ": " + message);
                // Optional: if you want to stop the operation:
                // throw new WebApplicationException(message, status);

                // Or continue with the next tenant
            }
        });
    }

    /**
     * Update an existing tenant.
     */

    @Transactional
    public TenantResponseDto updateTenant(String id, TenantRequestDto request) {
        var decryptedSecret = encryptUtil.decrypt(encryptedSecret);
        var client = argoWebApiClientFactory.buildClient(webapi);

        try {
            var image = request.info.image;
            if (image != null && image.startsWith("data:image/")) {
                var imageUrl = handleImage(request);
                request.info.image = imageUrl;
            }
            // If not Base64, leave image as-is (null or external URL)

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // ------------------------------
        // 1. Get previous remote state
        // ------------------------------
        TenantRequestDto previousRemoteState = null;
        try {
            var remoteExisting = client.getTenant(decryptedSecret, id);
            previousRemoteState = TenantMapper.INSTANCE.webApiTenantToTenantRequestDto(
                    remoteExisting.getData().get(0).getInfo()
            );
        } catch (Exception e) {
            throw new WebApplicationException("Cannot fetch remote state for rollback", 500);
        }

        // ------------------------------
        // 2. Update remote API first
        // ------------------------------
        try {
            client.updateTenant(id, decryptedSecret, request);
        } catch (Exception e) {
            throw new WebApplicationException("Remote API update failed: " + e.getMessage(), 502);
        }

        // ------------------------------
        // 3. Now try DB update
        // ------------------------------
        var tenant = tenantRepository.findById(id);
        if (tenant == null) {
            // rollback remote because DB tenant missing
            client.updateTenant(id, decryptedSecret, previousRemoteState);
            throw new WebApplicationException("Tenant not found in DB", 404);
        }

        try {
            TenantMapper.INSTANCE.updateToTenant(request, tenant);
            tenantRepository.persist(tenant);  // may fail
            tenantRepository.flush();          // force DB error here
        } catch (Exception dbException) {

            // ------------------------------
            // 4. DB FAILED → rollback remote
            // ------------------------------
            try {
                client.updateTenant(id, decryptedSecret, previousRemoteState);
            } catch (Exception rollbackEx) {
                throw new WebApplicationException("DB failed AND remote rollback failed: " + rollbackEx.getMessage(), 500);
            }

            throw new WebApplicationException( "DB update failed, remote rolled back: " + dbException.getMessage(), 500);
        }

        // All succeeded
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
    public PageResource<TenantResponseDto> getTenantsByPageAndSize(int page, int size, UriInfo uriInfo, String search,String sort, String order) {

        var tenants = tenantRepository.fetchTenantsByPageAndSize(page, size, search,sort,order);
        return new PageResource<>(tenants, TenantMapper.INSTANCE.tenantsToDtos(tenants.list()), uriInfo);

    }


    private String handleImage(TenantRequestDto request) throws IOException {

        var image = request.info.image;
        imageUploadUtil.validateBase64Image(image);
        var savedPath = imageUploadUtil.saveBase64Image(baseUploadTenantsImagesDir, image, request.info.name, "/logos/");

        return apiServerUrl + savedPath;
    }
}