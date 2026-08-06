package org.grnet.status.dtos.tenant.node;

import java.math.BigDecimal;
import java.util.List;

public class WebApiNodeMonitoringMetricResponse {

    public List<Data> data;

    public static class Data {
        public String name;
        public List<Result> results;
    }

    public static class Result {
        public String date;
        public BigDecimal availability;
        public BigDecimal reliability;
        public BigDecimal uptime;
    }
}