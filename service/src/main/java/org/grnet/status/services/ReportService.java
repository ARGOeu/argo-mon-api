package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.grnet.status.dtos.encrypt.EncryptRequestDto;
import org.grnet.status.dtos.encrypt.EncryptResponseDto;
import org.grnet.status.dtos.report.ReportRequestDto;
import org.grnet.status.dtos.report.ReportResponseDto;
import org.grnet.status.exceptions.BadRequestException;
import org.grnet.status.services.clients.ArgoWebApiClient;
import org.grnet.status.services.utils.EncryptUtil;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ReportService {

    private static final Logger LOG = Logger.getLogger(ReportService.class);

    @Inject
    EncryptUtil encryptUtil;

    @ConfigProperty(name = "api.status.encrypt.secret")
    String encryptSecret;

    /**
     * Fetches a list of reports from the ARGO Web API.
     *
     * @param request The report request containing the API URL and encrypted secret.
     * @return A list of report DTOs.
     */
    public List<ReportResponseDto> fetchReports(ReportRequestDto request) {
        var decryptedSecret = decrypt(request.secret);

        LOG.info("Building ARGO Web API client...");
        var client = RestClientBuilder.newBuilder()
                .baseUri(buildUri(request.api))
                .build(ArgoWebApiClient.class);

        var response = client.fetchReports(decryptedSecret);

        var list = new ArrayList<ReportResponseDto>();
        if (response != null && response.data != null) {
            for (var item : response.data) {
                if (item.info != null) {
                    var dto = new ReportResponseDto();
                    dto.name = item.info.name;
                    dto.description = item.info.description;
                    list.add(dto);
                }
            }
        }
        return list;
    }


    /**
     * Encrypts a plain-text secret.
     *
     * @param request The request containing the plain secret.
     * @return The encrypted secret DTO.
     */
    public EncryptResponseDto encrypt(EncryptRequestDto request) {

            var encrypted = encryptUtil.encrypt(request.secret, encryptSecret);
            var dto = new EncryptResponseDto();
            dto.secret = encrypted;

            return dto;
    }

    /**
     * Decrypts an encrypted secret.
     *
     * @param encryptedSecret The encrypted secret value.
     * @return The decrypted plain secret.
     */
    public String decrypt(String encryptedSecret) {

        return encryptUtil.decrypt(encryptedSecret, encryptSecret);
    }


    /**
     * Validates and builds the URI or throws IllegalArgumentException.
     */
    /**
     * Validates and builds the URI or throws BadRequestException.
     */
    private URI buildUri(String apiUrl) {
        try {
            var uri = new URI(apiUrl);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new BadRequestException(apiUrl,
                        Set.of("URL must include scheme and host (e.g. http://example.com)"));
            }
            return uri;
        } catch (URISyntaxException e) {
            throw new BadRequestException(apiUrl,
                    Set.of("The URL is not correctly formatted."));
        }
    }



}
