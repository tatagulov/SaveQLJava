package io.tatagulov.saveql.api;

import io.tatagulov.saveql.schema.S1;
import io.tatagulov.saveql.schema.TableDef1;
import io.tatagulov.saveql.schema.Values;

public final class DeleteCommand1<R, T1, T extends TableDef1<R, T1>> extends DeleteCommand<R, S1, T> {
    public DeleteCommand1(T table) {
        super(table);
    }

    public DeleteTerminalCommand<R, T> byId(T1 key1) {
        return super.byId(Values.of(key1));
    }
}
