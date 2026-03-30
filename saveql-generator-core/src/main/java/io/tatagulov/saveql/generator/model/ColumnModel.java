package io.tatagulov.saveql.generator.model;

import java.util.Objects;

public record ColumnModel(
        String columnName,
        String fieldName,
        String javaTypeName,
        int jdbcType,
        String typeName,
        boolean nullable,
        boolean hasDefaultValue,
        boolean generated,
        int ordinalPosition
) {
    public ColumnModel {
        Objects.requireNonNull(columnName, "columnName");
        Objects.requireNonNull(fieldName, "fieldName");
        Objects.requireNonNull(javaTypeName, "javaTypeName");
        Objects.requireNonNull(typeName, "typeName");
    }
}
