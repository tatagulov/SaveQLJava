package io.tatagulov.saveql.expr;

import java.util.Objects;

public final class BinaryMathExpr<N extends Number> implements SimpleExpr<N> {
    private final SimpleExpr<N> left;
    private final MathOp op;
    private final SimpleExpr<N> right;

    public BinaryMathExpr(SimpleExpr<N> left, MathOp op, SimpleExpr<N> right) {
        this.left = Objects.requireNonNull(left, "left");
        this.op = Objects.requireNonNull(op, "op");
        this.right = Objects.requireNonNull(right, "right");
    }

    public SimpleExpr<N> left() {
        return left;
    }

    public MathOp op() {
        return op;
    }

    public SimpleExpr<N> right() {
        return right;
    }

    @Override
    public Class<N> getType() {
        return left.getType();
    }
}
