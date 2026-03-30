package io.tatagulov.saveql;

import io.tatagulov.saveql.api.DSL;
import io.tatagulov.saveql.schema.Values;
import io.tatagulov.saveql.sql.CompiledSql;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeleteDslTest {
    @Test
    void compilesDeleteByIdSql() {
        UUID id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        CompiledSql compiled = DSL.delete(TestSchema.USERS)
                .byId(id)
                .compile();

        assertEquals("delete from users where users.id = ?", compiled.sql());
        assertEquals(List.of(id), compiled.params());
    }

    @Test
    void compilesDeleteByIdSqlWithValuesFallback() {
        UUID id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        CompiledSql compiled = DSL.delete(TestSchema.USERS)
                .byId(Values.of(id))
                .compile();

        assertEquals("delete from users where users.id = ?", compiled.sql());
        assertEquals(List.of(id), compiled.params());
    }

    @Test
    void compilesBulkDeleteSql() {
        TestSchema.OrdersTable o = TestSchema.ORDERS;
        CompiledSql compiled = DSL.delete(o)
                .where(o.price.lt(new BigDecimal("10.00")))
                .compile();

        assertEquals("delete from orders where orders.price < ?", compiled.sql());
        assertEquals(List.of(new BigDecimal("10.00")), compiled.params());
    }
}
