package io.tatagulov.saveql.expr;

public final class AggregateRules {
    private AggregateRules() {
    }

    public static boolean isAggregateExpr(Expr<?> expr) {

        if (expr instanceof AliasedExprLike<?> aliased) {
            return isAggregateExpr(aliased.expr());
        }
        if (expr instanceof AggregateExpr) {
            return true;
        }
        if (expr instanceof BinaryMathExpr<?> math) {
            return isAggregateExpr(math.left()) || isAggregateExpr(math.right());
        }
        if (expr instanceof FunctionExpr<?> fn) {
            return fn.args().stream().anyMatch(AggregateRules::isAggregateExpr);
        }
        if (expr instanceof CaseWhenExpr<?> caseWhen) {
            boolean inBranches = caseWhen.branches().stream()
                    .anyMatch(b -> isAggregateExpr(b.condition()) || isAggregateExpr(b.value()));
            if (inBranches) {
                return true;
            }
            return caseWhen.elseExpr() != null && isAggregateExpr(caseWhen.elseExpr());
        }
        return false;
    }
}
