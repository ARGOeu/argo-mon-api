package org.grnet.status.enums.resources;

import org.grnet.endpoint.scanner.runtime.ApiResource;
public enum PageResource implements ApiResource {

    PAGE;
    @Override
    public String resourceName() {
        return "Page";
    }
}

