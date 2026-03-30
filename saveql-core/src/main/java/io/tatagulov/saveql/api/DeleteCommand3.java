package io.tatagulov.saveql.api;

import io.tatagulov.saveql.schema.S3;
import io.tatagulov.saveql.schema.TableDef3;
import io.tatagulov.saveql.schema.Values;

public final class DeleteCommand3<R, T1, T2, T3, T extends TableDef3<R, T1, T2, T3>> extends DeleteCommand<R, S3, T> {
    public DeleteCommand3(T table) {
        super(table);
    }

    public DeleteTerminalCommand<R, T> byId(T1 key1, T2 key2, T3 key3) {
        return super.byId(Values.of(key1, key2, key3));
    }
}
