package io.tatagulov.saveql.api;

import io.tatagulov.saveql.schema.ColumnDef;
import io.tatagulov.saveql.schema.S2;
import io.tatagulov.saveql.schema.TableDef2;
import io.tatagulov.saveql.schema.Values;

public final class UpdateCommand2<R, T1, T2, T extends TableDef2<R, T1, T2>> extends UpdateCommand<R, S2, T> {
    public UpdateCommand2(T table) {
        super(table);
    }

    @Override
    public <V> UpdateCommand2<R, T1, T2, T> set(ColumnDef<T, V> column, V value) {
        super.set(column, value);
        return this;
    }

    public UpdateTerminalCommand<R, T> byId(T1 key1, T2 key2) {
        return super.byId(Values.of(key1, key2));
    }
}
