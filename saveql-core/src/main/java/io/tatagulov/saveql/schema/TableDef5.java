package io.tatagulov.saveql.schema;

public abstract class TableDef5<R, T1, T2, T3, T4, T5> extends TableDef<R, S5> implements TableLike5<R, T1, T2, T3, T4, T5> {
    protected TableDef5(String name, String alias) {
        super(name, alias);
    }

    @Override
    public abstract Cols5<R, T1, T2, T3, T4, T5> primaryKeyCols();
}
