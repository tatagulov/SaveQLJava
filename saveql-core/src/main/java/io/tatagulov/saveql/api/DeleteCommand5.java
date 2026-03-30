package io.tatagulov.saveql.api;

import io.tatagulov.saveql.schema.S5;
import io.tatagulov.saveql.schema.TableDef5;
import io.tatagulov.saveql.schema.Values;

public final class DeleteCommand5<R, T1, T2, T3, T4, T5, T extends TableDef5<R, T1, T2, T3, T4, T5>> extends DeleteCommand<R, S5, T> {
    public DeleteCommand5(T table) {
        super(table);
    }

    public DeleteTerminalCommand<R, T> byId(T1 key1, T2 key2, T3 key3, T4 key4, T5 key5) {
        return super.byId(Values.of(key1, key2, key3, key4, key5));
    }
}
