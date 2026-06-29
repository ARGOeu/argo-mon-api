package org.grnet.status.enums.resources;

import org.grnet.endpoint.scanner.runtime.ApiResource;
public enum ReportsResource implements ApiResource {

    REPORTS;
    @Override
    public String resourceName() {
        return "Reports";
    }
}



