package io.tatagulov.saveql.api;

import io.tatagulov.saveql.schema.ColumnDef;
import io.tatagulov.saveql.schema.S3;
import io.tatagulov.saveql.schema.TableDef3;
import io.tatagulov.saveql.schema.Values;

public final class UpdateCommand3<R, T1, T2, T3, T extends TableDef3<R, T1, T2, T3>> extends UpdateCommand<R, S3, T> {
    public UpdateCommand3(T table) {
        super(table);
    }

    @Override
    public <V> UpdateCommand3<R, T1, T2, T3, T> set(ColumnDef<T, V> column, V value) {
        super.set(column, value);
        return this;
    }

    public UpdateTerminalCommand<R, T> byId(T1 key1, T2 key2, T3 key3) {
        return super.byId(Values.of(key1, key2, key3));
    }
}
