package org.grnet.status.enums;

public enum ThemeOption {

    THEME_1("theme_1"),
    THEME_2("theme_2");

    private final String value;

    ThemeOption(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isValid(String value) {
        for (ThemeOption option : values()) {
            if (option.value.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}