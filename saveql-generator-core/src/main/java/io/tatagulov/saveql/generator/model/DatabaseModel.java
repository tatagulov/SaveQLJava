package io.tatagulov.saveql.generator.model;

import java.util.List;
import java.util.Objects;

public record DatabaseModel(List<TableModel> tables) {
    public DatabaseModel {
        Objects.requireNonNull(tables, "tables");
    }
}
