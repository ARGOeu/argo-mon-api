package org.grnet.status.enums.resources;

import org.grnet.endpoint.scanner.runtime.ApiResource;
public enum OperationsProfileResource implements ApiResource {

    OPERATIONS_PROFILE;
    @Override
    public String resourceName() {
        return "Operations_Profile";
    }
}


