package org.grnet.status.dtos.tenant.node;

import java.util.List;

public class WebApiNodeMonitoringMetricResponse {

    public List<Data> data;

    public static class Data {
        public String name;
        public List<Result> results;
    }

    public static class Result {
        public String date;
        public String availability;
        public String reliability;
        public String uptime;
    }
}

