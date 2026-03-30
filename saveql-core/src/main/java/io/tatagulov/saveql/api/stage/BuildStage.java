package io.tatagulov.saveql.api.stage;

import io.tatagulov.saveql.query.Query;

public interface BuildStage<RowType> {
    Query<RowType> build();
}
