package org.grnet.status.dtos.tenant.webapi;

import java.util.List;

public class TenantWebApiSupergroupsResponse {

    public List<SupergroupsResultData> results;

    public static class SupergroupsResultData {

        public String name;
        public String type;

        public List<SupergroupResults> results;
    }

    public static class SupergroupResults {
        public String timestamp;
        public String availability;
        public String reliability;
    }
}