package io.tatagulov.saveql.expr;

import java.util.List;
import java.util.Objects;

public record CombinePredicate(PredicateOp predicateOp, List<PredicateExpr> terms) implements PredicateExpr {

    public CombinePredicate(PredicateOp predicateOp, List<PredicateExpr> terms) {
        this.predicateOp = Objects.requireNonNull(predicateOp,"op");
        this.terms = List.copyOf(Objects.requireNonNull(terms, "terms"));
    }
}
