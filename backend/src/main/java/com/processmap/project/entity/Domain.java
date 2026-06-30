package com.processmap.project.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
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

    @Converter(autoApply = true)
    public static class DomainConverter implements AttributeConverter<Domain, String> {
        @Override
        public String convertToDatabaseColumn(Domain attribute) {
            return attribute != null ? attribute.name().toLowerCase() : null;
        }

        @Override
        public Domain convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.trim().isEmpty()) {
                return null;
            }
            Domain domain = Domain.fromString(dbData);
            return domain != null ? domain : Domain.INDUSTRIAL;
        }
    }
}
