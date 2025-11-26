package org.grnet.status.enums;

public enum ArgoItemStatusEnum {
    CRITICAL,
    OK,
    MISSING,
    WARNING,
    DOWNTIME,
    UNKNOWN;

    public static boolean isValid(String status) {
        if (status == null) return false;
        try {
            ArgoItemStatusEnum.valueOf(status);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}