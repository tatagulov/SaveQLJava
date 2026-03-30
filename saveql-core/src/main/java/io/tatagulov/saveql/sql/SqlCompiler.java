package io.tatagulov.saveql.sql;

import io.tatagulov.saveql.expr.*;
import io.tatagulov.saveql.query.JoinSpec;
import io.tatagulov.saveql.query.JoinType;
import io.tatagulov.saveql.query.NullOrder;
import io.tatagulov.saveql.query.OrderItem;
import io.tatagulov.saveql.query.Query;
import io.tatagulov.saveql.query.QueryValidationException;
import io.tatagulov.saveql.schema.AggViewDef;
import io.tatagulov.saveql.schema.ColumnLike;
import io.tatagulov.saveql.schema.TableDef;
import io.tatagulov.saveql.schema.TableLike;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class SqlCompiler {
    public CompiledSql compile(Query<?> query) {
        if (query.selectItems().isEmpty()) {
            throw new QueryValidationException("SELECT is required");
        }

        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();


        sql.append("SELECT ");
        sql.append(renderSelect(query, params));

        sql.append(" FROM ").append(renderTableUse(query.root(), List.of(), params));


        renderJoins(query.joins(), sql, params);


        PredicateSplit split = splitWhereAndHaving(query.where());

        if (split.where != null) {
            sql.append(" WHERE ").append(renderPredicate(split.where, params));
        }

        List<Expr<?>> groupByExprs = collectGroupByExpressions(query.selectItems(), split.having != null);
        if (!groupByExprs.isEmpty()) {
            sql.append(" GROUP BY ");
            sql.append(groupByExprs.stream()
                    .map(expr -> renderExpr(expr, new ArrayList<>()))
                    .collect(Collectors.joining(", ")));
        }

        if (split.having != null) {
            sql.append(" HAVING ").append(renderPredicate(split.having, params));
        }

        if (!query.orderItems().isEmpty()) {
            sql.append(" ORDER BY ");
            sql.append(query.orderItems().stream()
                    .map(item -> renderOrder(item, params))
                    .collect(Collectors.joining(", ")));
        }

        if (query.limit() != null) {
            sql.append(" LIMIT ?");
            params.add(query.limit());
        }

        if (query.offset() != null) {
            sql.append(" OFFSET ?");
            params.add(query.offset());
        }

        return new CompiledSql(sql.toString(), params);
    }

    private void renderJoins(List<JoinSpec<?, ?, ?>> joins, StringBuilder sql, List<Object> params) {
        for (var join : joins) {
            sql.append(' ');
            sql.append(join.type() == JoinType.LEFT ? "LEFT JOIN " : "JOIN ");
            sql.append(renderTableUse(join.to(), join.filters(), params));
            sql.append(" ON ");
            sql.append(join.conditions().stream()
                    .map(condition -> renderExpr(condition.from(), params) + " = " + renderExpr(condition.to(), params))
                    .collect(Collectors.joining(" AND ")));
            if (!join.subJoins().isEmpty()) {
                renderJoins(join.subJoins(), sql, params);
            }
        }
    }

    private String renderSelect(Query<?> query, List<Object> params) {
        return query.selectItems().stream()
                .map(item -> {
                    if (item instanceof AliasedExprLike<?> aliased) {
                        return renderExpr(aliased.expr(), params) + " AS " + aliased.alias();
                    }
                    return renderExpr(item, params);
                })
                .collect(Collectors.joining(", "));
    }

    private PredicateSplit splitWhereAndHaving(PredicateExpr predicate) {
        if (predicate == null) {
            return new PredicateSplit(null, null);
        }

        if (predicate instanceof CombinePredicate combine) {
            List<PredicateExpr> whereTerms = new ArrayList<>();
            List<PredicateExpr> havingTerms = new ArrayList<>();
            for (PredicateExpr term : combine.terms()) {
                if (containsAggregate(term)) {
                    havingTerms.add(term);
                } else {
                    whereTerms.add(term);
                }
            }

            if (combine.predicateOp() == PredicateOp.OR && !whereTerms.isEmpty() && !havingTerms.isEmpty()) {
                throw new IllegalArgumentException(
                        "can't split OR for where and having: " + predicate
                );
            }

            if (combine.predicateOp() == PredicateOp.OR) {
                return havingTerms.isEmpty()
                        ? new PredicateSplit(combine(whereTerms, PredicateOp.OR), null)
                        : new PredicateSplit(null, combine(havingTerms, PredicateOp.OR));
            }

            return new PredicateSplit(combine(whereTerms, PredicateOp.AND), combine(havingTerms, PredicateOp.AND));
        }

        if (containsAggregate(predicate)) {
            return new PredicateSplit(null, predicate);
        }
        return new PredicateSplit(predicate, null);
    }

    private PredicateExpr combine(List<PredicateExpr> terms, PredicateOp predicateOp) {
        if (terms.isEmpty()) {
            return null;
        }
        if (terms.size() == 1) {
            return terms.get(0);
        }
        return new CombinePredicate(predicateOp, terms);
    }

    private List<Expr<?>> collectGroupByExpressions(List<Expr<?>> items, boolean requireGroupBy) {
        boolean hasAggregate = items.stream().anyMatch(this::containsAggregate);
        if (!hasAggregate && !requireGroupBy) {
            return List.of();
        }
        List<Expr<?>> out = new ArrayList<>();
        for (Expr<?> item : items) {

            if (!containsAggregate(item) && !(unwrapAlias(item) instanceof LiteralExpr<?>)) {
                out.add(unwrapAlias(item));
            }
        }
        return out;
    }

    private Expr<?> unwrapAlias(Expr<?> expr) {
        if (expr instanceof AliasedExprLike<?> aliased) {
            return unwrapAlias(aliased.expr());
        }
        return expr;
    }

    private boolean containsAggregate(PredicateExpr predicate) {
        if (predicate instanceof ComparisonPredicate cmp) {
            return containsAggregate(cmp.left()) || containsAggregate(cmp.right());
        }
        if (predicate instanceof NullCheckPredicate nullCheck) {
            return containsAggregate(nullCheck.expr());
        }
        if (predicate instanceof BetweenPredicate between) {
            return containsAggregate(between.expr()) || containsAggregate(between.lower()) || containsAggregate(between.upper());
        }
        if (predicate instanceof InPredicate in) {
            if (containsAggregate(in.expr())) {
                return true;
            }
            return in.values().stream().anyMatch(this::containsAggregate);
        }
        if (predicate instanceof CombinePredicate and) {
            return and.terms().stream().anyMatch(this::containsAggregate);
        }
        return false;
    }

    private boolean containsAggregate(Expr<?> expr) {
        return AggregateRules.isAggregateExpr(expr);
    }

    private String renderOrder(OrderItem order, List<Object> params) {
        String rendered = renderExpr(order.expr(), params) + " " + order.direction().name();
        if (order.nullOrder() == NullOrder.NULLS_FIRST) {
            return rendered + " NULLS FIRST";
        }
        if (order.nullOrder() == NullOrder.NULLS_LAST) {
            return rendered + " NULLS LAST";
        }
        return rendered;
    }

    private String renderPredicate(PredicateExpr predicate, List<Object> params) {
        return renderPredicate(predicate, params, null);
    }

    private String renderPredicate(PredicateExpr predicate, List<Object> params, PredicateOp parentOp) {
        if (predicate instanceof ComparisonPredicate cmp) {
            return renderExpr(cmp.left(), params) + " " + cmp.op().sql() + " " + renderExpr(cmp.right(), params);
        }
        if (predicate instanceof CombinePredicate p) {
            String rendered = p.terms().stream()
                    .map(t -> renderPredicate(t, params, p.predicateOp()))
                    .collect(Collectors.joining(" " + p.predicateOp().sql() + " "));
            if (needsParentheses(parentOp, p.predicateOp())) {
                return "(" + rendered + ")";
            }
            return rendered;
        }
        if (predicate instanceof NullCheckPredicate nullCheck) {
            return renderExpr(nullCheck.expr(), params) + " " + nullCheck.op().sql();
        }
        if (predicate instanceof BetweenPredicate between) {
            return renderExpr(between.expr(), params)
                    + (between.negated() ? " NOT BETWEEN " : " BETWEEN ")
                    + renderExpr(between.lower(), params)
                    + " AND "
                    + renderExpr(between.upper(), params);
        }
        if (predicate instanceof InPredicate in) {
            String values = in.values().stream().map(v -> renderExpr(v, params)).collect(Collectors.joining(", "));
            return renderExpr(in.expr(), params) + (in.negated() ? " NOT IN (" : " IN (") + values + ")";
        }
        throw new QueryValidationException("Unsupported predicate type: " + predicate.getClass().getName());
    }

    private boolean needsParentheses(PredicateOp parentOp, PredicateOp currentOp) {
        return parentOp == PredicateOp.AND && currentOp == PredicateOp.OR;
    }

    private String renderExpr(Expr<?> expr, List<Object> params) {
        if (expr instanceof ColumnLike<?, ?> col) {
            return col.qualifiedName();
        }
        if (expr instanceof LiteralExpr<?> lit) {
            params.add(lit.value());
            return "?";
        }
        if (expr instanceof FunctionExpr<?> fn) {
            if (fn.args().isEmpty()) {
                return fn.name() + "()";
            }
            String renderedArgs = fn.args().stream()
                    .map(arg -> renderExpr(arg, params))
                    .collect(Collectors.joining(", "));
            return fn.name() + "(" + renderedArgs + ")";
        }
        if (expr instanceof CaseWhenExpr<?> caseWhen) {
            StringBuilder out = new StringBuilder("CASE");
            for (CaseWhenExpr.WhenThen<?> branch : caseWhen.branches()) {
                out.append(" WHEN ")
                        .append(renderPredicate(branch.condition(), params))
                        .append(" THEN ")
                        .append(renderExpr(branch.value(), params));
            }
            if (caseWhen.elseExpr() != null) {
                out.append(" ELSE ").append(renderExpr(caseWhen.elseExpr(), params));
            }
            out.append(" END");
            return out.toString();
        }
        if (expr instanceof BinaryMathExpr<?> math) {
            return "(" + renderExpr(math.left(), params) + " " + math.op().sql() + " " + renderExpr(math.right(), params) + ")";
        }
        if (expr instanceof AliasedExprLike<?> aliased) {
            return renderExpr(aliased.expr(), params);
        }
        throw new QueryValidationException("Unsupported expression type: " + expr.getClass().getName());
    }

    private String renderTableUse(TableLike<?, ?> table, List<? extends PredicateExpr> filters, List<Object> params) {
        if (table instanceof AggViewDef<?, ?, ?> view) {
            return renderAggView(view, filters, params);
        }
        if (table instanceof TableDef<?, ?> tableDef) {
            if (tableDef.hasAlias()) {
                return tableDef.name() + " " + tableDef.alias();
            }
            return tableDef.name();
        }
        throw new QueryValidationException("Unsupported table type: " + table.getClass().getName());
    }

    private static String renderColumnRef(TableLike<?, ?> use, String columnName) {
        return use.qualifier() + "." + columnName;
    }

    private <VR, SR> String renderAggView(AggViewDef<VR, SR, ?> view, List<? extends PredicateExpr> filters, List<Object> params) {
        StringBuilder out = new StringBuilder();
        out.append("(SELECT ");
        out.append(view.projections().stream()
                .map(p -> renderExpr(p.expr(), params) + " AS " + p.column().name())
                .collect(Collectors.joining(", ")));
        out.append(" FROM ").append(renderTableUse(view.source(), List.of(), params));
        if (!filters.isEmpty()) {
            out.append(" WHERE ");
            out.append(filters.stream()
                    .map(p -> "(" + renderPredicate(p, params) + ")")
                    .collect(Collectors.joining(" AND ")));
        }
        out.append(" GROUP BY ");

        List<String> pkColumns = new ArrayList<>();
        view.primaryKeyCols().forEach(col -> pkColumns.add(renderColumnRef(view.source(), col.name())));

        out.append(String.join(", ", pkColumns));
        out.append(") ").append(view.alias());
        return out.toString();
    }

    private record PredicateSplit(PredicateExpr where, PredicateExpr having) {
    }
}
