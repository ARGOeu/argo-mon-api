package org.grnet.status.dtos.status;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TenantWebApiGroupStatusTimelineResponse {

    @JsonProperty("groups")
    public List<Group> groups;

    public static class Group {

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