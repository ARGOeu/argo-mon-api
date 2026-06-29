package org.grnet.status.enums.resources;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum FeedType {

    INTERNAL("internal"),
    EXTERNAL("external"),
    CSV("CSV"),
    DESY_MARKETPLACE("desy-marketplace"),
    EOSC_SERVICE_CATALOG("eosc-service-catalog");

    private final String value;

    FeedType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
