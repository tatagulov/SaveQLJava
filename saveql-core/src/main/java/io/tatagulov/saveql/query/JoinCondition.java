package io.tatagulov.saveql.query;

import io.tatagulov.saveql.schema.ColumnLike;
import java.util.Objects;

public record JoinCondition<L, R, T>(
        ColumnLike<L, T> from,
        ColumnLike<R, T> to
) {
    public JoinCondition {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
    }
}
