package io.tatagulov.saveql.schema;

public sealed interface Values<S extends Shape> permits Values1, Values2, Values3, Values4, Values5 {
    static <T1> Values1<T1> of(T1 value1) {
        return new Values1<>(value1);
    }

    static <T1, T2> Values2<T1, T2> of(T1 value1, T2 value2) {
        return new Values2<>(value1, value2);
    }

    static <T1, T2, T3> Values3<T1, T2, T3> of(T1 value1, T2 value2, T3 value3) {
        return new Values3<>(value1, value2, value3);
    }

    static <T1, T2, T3, T4> Values4<T1, T2, T3, T4> of(T1 value1, T2 value2, T3 value3, T4 value4) {
        return new Values4<>(value1, value2, value3, value4);
    }

    static <T1, T2, T3, T4, T5> Values5<T1, T2, T3, T4, T5> of(
            T1 value1,
            T2 value2,
            T3 value3,
            T4 value4,
            T5 value5
    ) {
        return new Values5<>(value1, value2, value3, value4, value5);
    }
}
