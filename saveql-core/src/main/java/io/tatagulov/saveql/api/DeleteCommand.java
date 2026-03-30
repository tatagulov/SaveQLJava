package io.tatagulov.saveql.api;

import io.tatagulov.saveql.expr.ComparisonPredicate;
import io.tatagulov.saveql.expr.CombinePredicate;
import io.tatagulov.saveql.expr.LiteralExpr;
import io.tatagulov.saveql.expr.PredicateExpr;
import io.tatagulov.saveql.expr.PredicateOp;
import io.tatagulov.saveql.schema.ColumnLike;
import io.tatagulov.saveql.schema.ColumnValueConsumer;
import io.tatagulov.saveql.schema.Shape;
import io.tatagulov.saveql.schema.TableDef;
import io.tatagulov.saveql.schema.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeleteCommand<R, S extends Shape, T extends TableDef<R, S>> {
    private final T table;

    public DeleteCommand(T table) {
        this.table = Objects.requireNonNull(table, "table");
    }

    public DeleteTerminalCommand<R, T> where(PredicateExpr predicate) {
        return DeleteTerminalCommand.where(table, predicate);
    }

    public DeleteTerminalCommand<R, T> byId(Values<S> key) {
        List<PredicateExpr> keyPredicates = new ArrayList<>();
        table.primaryKeyCols().zip(key, new ColumnValueConsumer<>() {
            @Override
            public <TYPE> void each(ColumnLike<R, TYPE> leftColumn, TYPE value) {
                ComparisonPredicate predicate = leftColumn.eq(new LiteralExpr<>(value));
                keyPredicates.add(predicate);
            }
        });
        return DeleteTerminalCommand.where(table, new CombinePredicate(PredicateOp.AND, keyPredicates));
    }
}
