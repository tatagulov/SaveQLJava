package io.tatagulov.saveql.api;

import io.tatagulov.saveql.schema.ColumnDef;
import io.tatagulov.saveql.schema.S5;
import io.tatagulov.saveql.schema.TableDef5;
import io.tatagulov.saveql.schema.Values;

public final class UpdateCommand5<R, T1, T2, T3, T4, T5, T extends TableDef5<R, T1, T2, T3, T4, T5>> extends UpdateCommand<R, S5, T> {
    public UpdateCommand5(T table) {
        super(table);
    }

    @Override
    public <V> UpdateCommand5<R, T1, T2, T3, T4, T5, T> set(ColumnDef<T, V> column, V value) {
        super.set(column, value);
        return this;
    }

    public UpdateTerminalCommand<R, T> byId(T1 key1, T2 key2, T3 key3, T4 key4, T5 key5) {
        return super.byId(Values.of(key1, key2, key3, key4, key5));
    }
}
