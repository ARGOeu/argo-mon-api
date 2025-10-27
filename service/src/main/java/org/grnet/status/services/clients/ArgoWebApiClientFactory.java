package org.grnet.status.services.clients;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.grnet.status.services.utils.UriUtil;

/**
 * Builds instances of ArgoWebApiClient for a given base URI.
 */
@ApplicationScoped
public class ArgoWebApiClientFactory {

    @Inject
    UriUtil uriUtil;

    public ArgoWebApiClient buildClient(String api) {

        var baseUri = uriUtil.buildUri(api);
        return RestClientBuilder.newBuilder()
                .baseUri(baseUri)
                .build(ArgoWebApiClient.class);
    }
}
