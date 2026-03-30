package io.tatagulov.saveql.schema;

public interface TableLike<R, S extends Shape> {

    String qualifier();

    Cols<S, R> primaryKeyCols();
}
