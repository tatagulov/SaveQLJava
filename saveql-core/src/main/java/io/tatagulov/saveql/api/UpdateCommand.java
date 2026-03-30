package io.tatagulov.saveql.api;

import io.tatagulov.saveql.expr.*;
import io.tatagulov.saveql.query.QueryValidationException;
import io.tatagulov.saveql.schema.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class UpdateCommand<R, S extends Shape, T extends TableDef<R, S>> {
    private final T table;
    private final LinkedHashMap<ColumnDef<T, ?>, Object> values = new LinkedHashMap<>();

    public UpdateCommand(T table) {
        this.table = Objects.requireNonNull(table, "table");
    }

    public <V> UpdateCommand<R, S, T> set(ColumnDef<T, V> column, V value) {
        Objects.requireNonNull(column, "column");
        if (column.tableLike() != table) {
            throw new QueryValidationException("Column " + column.qualifiedName() + " does not belong to update table " + table.name());
        }
        if (column.isGenerated()) {
            throw new QueryValidationException("Column " + column.name() + " is generated and cannot be updated explicitly");
        }
        values.put(column, value);
        return this;
    }

    public UpdateTerminalCommand<R, T> where(PredicateExpr predicate) {
        return UpdateTerminalCommand.where(table, values, predicate);
    }

    public UpdateTerminalCommand<R, T> byId(Values<S> key) {
        List<PredicateExpr> keyPredicates = new ArrayList<>();
        table.primaryKeyCols().zip(key, new ColumnValueConsumer<>() {
            @Override
            public <TYPE> void each(ColumnLike<R, TYPE> leftColumn, TYPE value) {
                ComparisonPredicate predicate = leftColumn.eq(new LiteralExpr<>(value));
                keyPredicates.add(predicate);
            }
        });
        return UpdateTerminalCommand.where(table, values, new CombinePredicate(PredicateOp.AND, keyPredicates));
    }
}
