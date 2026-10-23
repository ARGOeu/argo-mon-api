package org.grnet.status.dtos.status;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class TenantWebApiMetricStatusTimelineResponse {

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

        @JsonProperty("endpoints")
        public List<Endpoint> endpoints;
    }

    public static class Endpoint {

        @JsonProperty("name")
        public String name;

        @JsonProperty("info")
        public Map<String, String> info;

        @JsonProperty("metrics")
        public List<Metric> metrics;
    }

    public static class Metric {

        @JsonProperty("name")
        public String name;

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