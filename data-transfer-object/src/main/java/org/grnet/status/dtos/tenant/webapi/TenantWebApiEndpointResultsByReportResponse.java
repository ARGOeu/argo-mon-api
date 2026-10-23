package org.grnet.status.dtos.tenant.webapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import jdk.jfr.Name;

import java.util.List;
import java.util.Map;

public class TenantWebApiEndpointResultsByReportResponse {

    public List<GroupData> results;

    public static class GroupData {
        public String name;
        public String type;
        @JsonProperty("service-types")
        public List<ServiceTypesData> serviceTypes;
    }

    public static class ServiceTypesData {
        public String name;
        public String type;
        public List<EndpointData> endpoints;
    }

    public static class EndpointData {
        public String name;
        public String type;
        public Map<String, String> info;
        public List<ResultData> results;
    }

    public static class ResultData {
        public String timestamp;
        public String availability;
        public String reliability;
        public String unknown;
        public String uptime;
        public String downtime;
    }
}
