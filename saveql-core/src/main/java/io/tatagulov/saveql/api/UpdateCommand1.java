package io.tatagulov.saveql.api;

import io.tatagulov.saveql.schema.ColumnDef;
import io.tatagulov.saveql.schema.S1;
import io.tatagulov.saveql.schema.TableDef1;
import io.tatagulov.saveql.schema.Values;

public final class UpdateCommand1<R, T1, T extends TableDef1<R, T1>> extends UpdateCommand<R, S1, T> {
    public UpdateCommand1(T table) {
        super(table);
    }

    @Override
    public <V> UpdateCommand1<R, T1, T> set(ColumnDef<T, V> column, V value) {
        super.set(column, value);
        return this;
    }

    public UpdateTerminalCommand<R, T> byId(T1 key1) {
        return super.byId(Values.of(key1));
    }
}
