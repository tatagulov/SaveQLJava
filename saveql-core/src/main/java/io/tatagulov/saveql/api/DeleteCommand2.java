package io.tatagulov.saveql.api;

import io.tatagulov.saveql.schema.S2;
import io.tatagulov.saveql.schema.TableDef2;
import io.tatagulov.saveql.schema.Values;

public final class DeleteCommand2<R, T1, T2, T extends TableDef2<R, T1, T2>> extends DeleteCommand<R, S2, T> {
    public DeleteCommand2(T table) {
        super(table);
    }

    public DeleteTerminalCommand<R, T> byId(T1 key1, T2 key2) {
        return super.byId(Values.of(key1, key2));
    }
}
