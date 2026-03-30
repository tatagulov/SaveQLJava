package io.tatagulov.saveql.expr;

import java.util.ArrayList;
import java.util.List;

public interface PredicateExpr extends Expr<Boolean> {
    default CombinePredicate and(PredicateExpr... otherTerms) {
        List<PredicateExpr> terms = new ArrayList<>();
        terms.add(this);
        terms.addAll(List.of(otherTerms));
        return new CombinePredicate(PredicateOp.AND, terms);
    }

    default CombinePredicate or(PredicateExpr... otherTerms) {
        List<PredicateExpr> terms = new ArrayList<>();
        terms.add(this);
        terms.addAll(List.of(otherTerms));
        return new CombinePredicate(PredicateOp.OR, terms);
    }

    @Override
    default Class<Boolean> getType() {
        return Boolean.class;
    }
}
