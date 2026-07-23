package org.grnet.status.dtos.tenant.webapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class TenantWebApiGroupEndpointResultsByReportResponse {

    public List<GroupData> results;
    @Getter
    public static class GroupData {
        public String name;
        public String type;
        @JsonProperty("service-types")
        public List<ServiceTypeData> serviceTypes;
    }

    @Getter
    public static class ServiceTypeData {
        public String name;
        public String type;
        public List<EndpointData> endpoints;
    }

    @Getter
    public static class EndpointData {
        public String name;
        public String type;
        public Map<String, String> info;
        public List<ResultData> results;
    }


    @Getter
    public static class ResultData {
        public String timestamp;
        public String availability;
        public String reliability;
        public String unknown;
        public String uptime;
        public String downtime;
    }
}
