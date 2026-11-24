package org.grnet.status.dtos.recomputations;
public record RecomputationElement(
        String metric,
        String hostname,
        String service,
        String group
) {}
