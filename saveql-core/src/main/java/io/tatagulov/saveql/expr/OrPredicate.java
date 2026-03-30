package io.tatagulov.saveql.expr;

import java.util.List;
import java.util.Objects;

public final class OrPredicate implements PredicateExpr {
    private final List<PredicateExpr> terms;

    public OrPredicate(List<PredicateExpr> terms) {
        this.terms = List.copyOf(Objects.requireNonNull(terms, "terms"));
    }

    public List<PredicateExpr> terms() {
        return terms;
    }
}
