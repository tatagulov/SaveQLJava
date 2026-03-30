package io.tatagulov.saveql.api;

import io.tatagulov.saveql.schema.ColumnDef;
import io.tatagulov.saveql.schema.S4;
import io.tatagulov.saveql.schema.TableDef4;
import io.tatagulov.saveql.schema.Values;

public final class UpdateCommand4<R, T1, T2, T3, T4, T extends TableDef4<R, T1, T2, T3, T4>> extends UpdateCommand<R, S4, T> {
    public UpdateCommand4(T table) {
        super(table);
    }

    @Override
    public <V> UpdateCommand4<R, T1, T2, T3, T4, T> set(ColumnDef<T, V> column, V value) {
        super.set(column, value);
        return this;
    }

    public UpdateTerminalCommand<R, T> byId(T1 key1, T2 key2, T3 key3, T4 key4) {
        return super.byId(Values.of(key1, key2, key3, key4));
    }
}
