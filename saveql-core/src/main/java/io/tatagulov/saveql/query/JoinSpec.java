package io.tatagulov.saveql.query;

import io.tatagulov.saveql.expr.PredicateExpr;
import io.tatagulov.saveql.schema.Shape;
import io.tatagulov.saveql.schema.TableLike;
import java.util.List;
import java.util.Objects;

public final class JoinSpec<L, R, S extends Shape> {
    private final JoinType type;
    private final TableLike<R, S> to;
    private final List<JoinCondition<L, R, ?>> conditions;
    private final List<PredicateExpr> filters;
    private final List<JoinSpec<?, ?, ?>> subJoins;

    public JoinSpec(
            JoinType type,
            TableLike<R, S> to,
            List<JoinCondition<L, R, ?>> conditions,
            List<PredicateExpr> filters,
            List<JoinSpec<?, ?, ?>> subJoins
    ) {
        this.type = Objects.requireNonNull(type, "type");
        this.to = Objects.requireNonNull(to, "to");
        this.conditions = List.copyOf(Objects.requireNonNull(conditions, "conditions"));
        if (this.conditions.isEmpty()) {
            throw new IllegalArgumentException("Join conditions must not be empty");
        }
        this.filters = List.copyOf(Objects.requireNonNull(filters, "filters"));
        this.subJoins = List.copyOf(Objects.requireNonNull(subJoins, "subJoins"));
    }

    public JoinType type() {
        return type;
    }

    public TableLike<R, S> to() {
        return to;
    }

    public List<JoinCondition<L, R, ?>> conditions() {
        return conditions;
    }

    public List<PredicateExpr> filters() {
        return filters;
    }

    public List<JoinSpec<?, ?, ?>> subJoins() {
        return subJoins;
    }
}
