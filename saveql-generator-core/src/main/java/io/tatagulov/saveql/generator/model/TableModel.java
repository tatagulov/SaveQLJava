package io.tatagulov.saveql.generator.model;

import java.util.List;
import java.util.Objects;

public record TableModel(
        String catalog,
        String schema,
        String tableName,
        String javaClassName,
        String constantName,
        List<ColumnModel> columns,
        PrimaryKeyModel primaryKey
) {
    public TableModel {
        Objects.requireNonNull(tableName, "tableName");
        Objects.requireNonNull(javaClassName, "javaClassName");
        Objects.requireNonNull(constantName, "constantName");
        Objects.requireNonNull(columns, "columns");
        Objects.requireNonNull(primaryKey, "primaryKey");
    }
}
