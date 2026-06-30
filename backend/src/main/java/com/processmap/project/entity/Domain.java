package com.processmap.project.entity;

import java.util.Locale;

public enum Domain {
    INDUSTRIAL,
    ELECTRICAL,
    HYDRAULIC;

    public static Domain fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Domain.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
