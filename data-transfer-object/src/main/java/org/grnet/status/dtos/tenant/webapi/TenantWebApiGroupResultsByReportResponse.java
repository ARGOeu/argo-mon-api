package org.grnet.status.dtos.tenant.webapi;

import java.util.List;

public class TenantWebApiGroupResultsByReportResponse {

    public List<GroupData> results;

    public static class GroupData {
        public String name;
        public String type;
        public List<GroupEndpoints> groups;
    }

    public static class GroupEndpoints {
        public String name;
        public String type;
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
