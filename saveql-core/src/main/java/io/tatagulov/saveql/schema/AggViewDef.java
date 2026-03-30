package io.tatagulov.saveql.schema;

import io.tatagulov.saveql.expr.AggregateRules;
import io.tatagulov.saveql.expr.Expr;
import io.tatagulov.saveql.expr.SimpleExpr;
import io.tatagulov.saveql.query.QueryValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AggViewDef<VR, SR, S extends Shape> implements TableLike<VR, S> {
    private final String alias;
    private final TableLike<SR, ?> source;
    private final Cols<S, VR> primaryKeyCols;
    private final List<Projection<?>> projections = new ArrayList<>();

    public AggViewDef(String alias, Cols<S, SR> primaryKeyCols) {
        this.alias = Objects.requireNonNull(alias, "alias");
        if (this.alias.isBlank()) {
            throw new QueryValidationException("View alias must be non-empty");
        }
        this.source = getSource(primaryKeyCols);
        this.primaryKeyCols = Objects.requireNonNull(primaryKeyCols, "primaryKeyCols").map(this::addPrimaryColumnFromSource);
    }

    private TableLike<SR, ?> getSource(Cols<S, SR> primaryKeyCols) {
        List<TableLike<SR, ?>> sourceTables = new ArrayList<>();

        primaryKeyCols.forEach(srColumnLike -> sourceTables.add(srColumnLike.tableLike()));

        if (sourceTables.isEmpty()) {
            throw new QueryValidationException("View must have at least one column from the source table");
        }
        return sourceTables.get(0);
    }

    private <T> ColumnDef<VR, T> addPrimaryColumnFromSource(ColumnLike<SR, T> column) {
        ColumnDef<VR, T> viewColumn = new ColumnDef<>(this, column.name(), column.getType(), true, false, false);
        this.projections.add(new Projection<>(column, viewColumn));
        return viewColumn;
    }

    public TableLike<SR, ?> source() {
        return source;
    }

    public List<Projection<?>> projections() {
        return List.copyOf(projections);
    }

    public String alias() {
        return alias;
    }

    @Override
    public String qualifier() {
        return alias;
    }

    @Override
    public Cols<S, VR> primaryKeyCols() {
        return primaryKeyCols;
    }

    public final <T> ColumnDef<VR, T> col(SimpleExpr<T> expr, String alias, Class<T> javaType) {
        Objects.requireNonNull(expr, "expr");
        if (!AggregateRules.isAggregateExpr(expr)) {
            throw new QueryValidationException("Aggregate view projection must contain aggregate expression: " + alias);
        }
        ColumnDef<VR, T> column = new ColumnDef<>(this, alias, javaType, true, false, false);
        projections.add(new Projection<>(expr, column));
        return column;
    }


    public record Projection<T>(Expr<T> expr, ColumnLike<?, T> column) {
    }
}
