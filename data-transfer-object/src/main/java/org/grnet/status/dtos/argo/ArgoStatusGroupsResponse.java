package org.grnet.status.dtos.argo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class ArgoStatusGroupsResponse {

    @JsonProperty("groups")
    public List<Group> groups;

    public static class Group {
        @JsonProperty("name")
        public String name;

        @JsonProperty("type")
        public String type;

        @JsonProperty("statuses")
        public List<Status> statuses;

        @JsonProperty("endpoints")
        public List<Endpoint> endpoints;
    }

    public static class Status {
        @JsonProperty("timestamp")
        public String timestamp;

        @JsonProperty("value")
        public String value;
    }

    public static class Endpoint {
        @JsonProperty("hostname")
        public String hostname;

        @JsonProperty("service")
        public String service;

        @JsonProperty("info")
        public Map<String, Object> info;

        @JsonProperty("statuses")
        public List<Status> statuses;
    }
}
