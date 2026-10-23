package org.grnet.status.enums.resources;

import org.grnet.endpoint.scanner.runtime.ApiResource;
public enum DowntimeResource implements ApiResource {

    DOWNTIME;
    @Override
    public String resourceName() {
        return "Downtime";
    }
}
