package io.tatagulov.saveql.api;

import io.tatagulov.saveql.schema.S4;
import io.tatagulov.saveql.schema.TableDef4;
import io.tatagulov.saveql.schema.Values;

public final class DeleteCommand4<R, T1, T2, T3, T4, T extends TableDef4<R, T1, T2, T3, T4>> extends DeleteCommand<R, S4, T> {
    public DeleteCommand4(T table) {
        super(table);
    }

    public DeleteTerminalCommand<R, T> byId(T1 key1, T2 key2, T3 key3, T4 key4) {
        return super.byId(Values.of(key1, key2, key3, key4));
    }
}
