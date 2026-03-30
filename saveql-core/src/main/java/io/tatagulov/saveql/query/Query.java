package io.tatagulov.saveql.query;

import io.tatagulov.saveql.expr.Expr;
import io.tatagulov.saveql.expr.PredicateExpr;
import io.tatagulov.saveql.schema.TableLike;

import java.util.*;

public final class Query<RootRow> {
    private final TableLike<RootRow, ?> root;
    private final List<JoinSpec<?, ?, ?>> joins = new ArrayList<>();
    private final List<Expr<?>> selectItems = new ArrayList<>();
    private final List<OrderItem> orderItems = new ArrayList<>();
    private PredicateExpr where;
    private Integer limit;
    private Integer offset;

    public Query(TableLike<RootRow, ?> root) {
        this.root = Objects.requireNonNull(root, "root");
        validateTableUses(List.of(root));
    }

    public TableLike<RootRow, ?> root() {
        return root;
    }

    public void addJoin(JoinSpec<?, ?, ?> join) {
        joins.add(Objects.requireNonNull(join, "join"));
        validateTableUses(allTableUses());
    }

    public Query<RootRow> select(Expr<?>... exprs) {
        if (!selectItems.isEmpty()) {
            throw new QueryValidationException("SELECT can only be defined once");
        }
        if (exprs.length == 0) {
            throw new QueryValidationException("SELECT requires at least one expression");
        }
        Collections.addAll(selectItems, exprs);
        return this;
    }

    public Query<RootRow> where(PredicateExpr predicate) {
        if (where != null) {
            throw new QueryValidationException("WHERE can only be defined once");
        }
        this.where = Objects.requireNonNull(predicate, "predicate");
        return this;
    }

    public Query<RootRow> orderBy(OrderItem... items) {
        if (!orderItems.isEmpty()) {
            throw new QueryValidationException("ORDER BY can only be defined once");
        }
        orderItems.addAll(List.of(items));
        return this;
    }

    public Query<RootRow> limit(int limit) {
        if (limit <= 0) {
            throw new QueryValidationException("LIMIT must be > 0");
        }
        if (this.limit != null) {
            throw new QueryValidationException("LIMIT can only be defined once");
        }
        this.limit = limit;
        return this;
    }

    public Query<RootRow> offset(int offset) {
        if (offset < 0) {
            throw new QueryValidationException("OFFSET must be >= 0");
        }
        if (limit == null) {
            throw new QueryValidationException("OFFSET requires LIMIT");
        }
        if (this.offset != null) {
            throw new QueryValidationException("OFFSET can only be defined once");
        }
        this.offset = offset;
        return this;
    }

    public List<JoinSpec<?, ?, ?>> joins() {
        return List.copyOf(joins);
    }

    public List<Expr<?>> selectItems() {
        return List.copyOf(selectItems);
    }

    public PredicateExpr where() {
        return where;
    }

    public List<OrderItem> orderItems() {
        return List.copyOf(orderItems);
    }

    public Integer limit() {
        return limit;
    }

    public Integer offset() {
        return offset;
    }

    private List<TableLike<?, ?>> allTableUses() {
        List<TableLike<?, ?>> uses = new ArrayList<>();
        uses.add(root);
        collectTableUses(joins, uses);
        return uses;
    }

    private static void validateTableUses(List<TableLike<?, ?>> uses) {
        Set<String> qualifiers = new HashSet<>();
        for (TableLike<?, ?> table : uses) {
            if (!qualifiers.add(table.qualifier())) {
                throw new QueryValidationException(
                        "Table or alias " + table.qualifier() + " is used multiple times without alias"
                );
            }
        }
    }

    private void collectTableUses(List<JoinSpec<?, ?, ?>> joinSpecs, List<TableLike<?, ?>> out) {
        for (JoinSpec<?, ?, ?> join : joinSpecs) {
            out.add(join.to());
            if (!join.subJoins().isEmpty()) {
                collectTableUses(join.subJoins(), out);
            }
        }
    }
}
