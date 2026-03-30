package io.tatagulov.saveql.generator.model;

import java.util.List;
import java.util.Objects;

public record PrimaryKeyModel(List<String> columnNames) {
    public PrimaryKeyModel {
        Objects.requireNonNull(columnNames, "columnNames");
    }

    public int size() {
        return columnNames.size();
    }
}
