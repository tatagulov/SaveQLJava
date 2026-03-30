package io.tatagulov.saveql.sql;

import io.tatagulov.saveql.api.DeleteTerminalCommand;
import io.tatagulov.saveql.expr.AliasedExprLike;
import io.tatagulov.saveql.expr.BetweenPredicate;
import io.tatagulov.saveql.expr.BinaryMathExpr;
import io.tatagulov.saveql.expr.CaseWhenExpr;
import io.tatagulov.saveql.expr.CombinePredicate;
import io.tatagulov.saveql.expr.ComparisonPredicate;
import io.tatagulov.saveql.expr.Expr;
import io.tatagulov.saveql.expr.FunctionExpr;
import io.tatagulov.saveql.expr.InPredicate;
import io.tatagulov.saveql.expr.LiteralExpr;
import io.tatagulov.saveql.expr.NullCheckPredicate;
import io.tatagulov.saveql.expr.PredicateExpr;
import io.tatagulov.saveql.expr.PredicateOp;
import io.tatagulov.saveql.query.QueryValidationException;
import io.tatagulov.saveql.schema.ColumnLike;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class DeleteSqlCompiler {
    public CompiledSql compile(DeleteTerminalCommand<?, ?> command) {
        command.validate();
        List<Object> params = new ArrayList<>();
        String sql = "delete from " + command.table().name()
                     + " where " + renderPredicate(command.where(), params);
        return new CompiledSql(sql, params);
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
            if (parentOp == PredicateOp.AND && p.predicateOp() == PredicateOp.OR) {
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
            return fn.name() + "(" + fn.args().stream().map(arg -> renderExpr(arg, params)).collect(Collectors.joining(", ")) + ")";
        }
        if (expr instanceof AliasedExprLike<?> aliased) {
            return renderExpr(aliased.expr(), params);
        }
        if (expr instanceof BinaryMathExpr<?> math) {
            return renderExpr(math.left(), params) + " " + math.op().sql() + " " + renderExpr(math.right(), params);
        }
        if (expr instanceof CaseWhenExpr<?> caseWhen) {
            StringBuilder builder = new StringBuilder("CASE");
            for (CaseWhenExpr.WhenThen<?> branch : caseWhen.branches()) {
                builder.append(" WHEN ")
                        .append(renderPredicate(branch.condition(), params))
                        .append(" THEN ")
                        .append(renderExpr(branch.value(), params));
            }
            if (caseWhen.elseExpr() != null) {
                builder.append(" ELSE ").append(renderExpr(caseWhen.elseExpr(), params));
            }
            return builder.append(" END").toString();
        }
        throw new QueryValidationException("Unsupported expression type: " + expr.getClass().getName());
    }
}
