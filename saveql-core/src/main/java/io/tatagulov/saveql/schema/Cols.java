package io.tatagulov.saveql.schema;

import java.util.Objects;
import java.util.function.Consumer;

public interface Cols<S extends Shape, L>  {
    static <R, T1> Cols1<R, T1> of(ColumnLike<R, T1> c1) {
        return new Cols1<>(Objects.requireNonNull(c1, "c1"));
    }

    static <R, T1, T2> Cols2<R, T1, T2> of(ColumnLike<R, T1> c1, ColumnLike<R, T2> c2) {
        return new Cols2<>(
                Objects.requireNonNull(c1, "c1"),
                Objects.requireNonNull(c2, "c2")
        );
    }

    static <R, T1, T2, T3> Cols3<R, T1, T2, T3> of(
            ColumnLike<R, T1> c1,
            ColumnLike<R, T2> c2,
            ColumnLike<R, T3> c3
    ) {
        return new Cols3<>(
                Objects.requireNonNull(c1, "c1"),
                Objects.requireNonNull(c2, "c2"),
                Objects.requireNonNull(c3, "c3")
        );
    }

    static <R, T1, T2, T3, T4> Cols4<R, T1, T2, T3, T4> of(
            ColumnLike<R, T1> c1,
            ColumnLike<R, T2> c2,
            ColumnLike<R, T3> c3,
            ColumnLike<R, T4> c4
    ) {
        return new Cols4<>(
                Objects.requireNonNull(c1, "c1"),
                Objects.requireNonNull(c2, "c2"),
                Objects.requireNonNull(c3, "c3"),
                Objects.requireNonNull(c4, "c4")
        );
    }

    static <R, T1, T2, T3, T4, T5> Cols5<R, T1, T2, T3, T4, T5> of(
            ColumnLike<R, T1> c1,
            ColumnLike<R, T2> c2,
            ColumnLike<R, T3> c3,
            ColumnLike<R, T4> c4,
            ColumnLike<R, T5> c5
    ) {
        return new Cols5<>(
                Objects.requireNonNull(c1, "c1"),
                Objects.requireNonNull(c2, "c2"),
                Objects.requireNonNull(c3, "c3"),
                Objects.requireNonNull(c4, "c4"),
                Objects.requireNonNull(c5, "c5")
        );
    }

    <R> void zip(Cols<S, R> rightColumns, ColumnPairConsumer<L, R> consumer);

    void zip(Values<S> values, ColumnValueConsumer<L> consumer);

    void forEach(Consumer<ColumnLike<L, ?>> forEachFunc);
    <R> Cols<S, R> map(ColumnMapper<L, R> mapper);
}
