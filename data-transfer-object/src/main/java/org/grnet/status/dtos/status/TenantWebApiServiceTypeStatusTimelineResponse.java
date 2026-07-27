package org.grnet.status.dtos.status;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TenantWebApiServiceTypeStatusTimelineResponse {

    @JsonProperty("groups")
    public List<Group> groups;

    public static class Group {

        @JsonProperty("name")
        public String name;

        @JsonProperty("type")
        public String type;

        @JsonProperty("service-types")
        public List<ServiceType> serviceTypes;
    }

    public static class ServiceType {

        @JsonProperty("name")
        public String name;

        @JsonProperty("type")
        public String type;

        @JsonProperty("statuses")
        public List<Status> statuses;
    }

    public static class Status {

        @JsonProperty("timestamp")
        public String timestamp;

        @JsonProperty("value")
        public String value;
    }
}
