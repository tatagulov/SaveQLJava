package io.tatagulov.saveql;

import io.tatagulov.saveql.api.DSL;
import io.tatagulov.saveql.query.QueryValidationException;
import io.tatagulov.saveql.schema.Values;
import io.tatagulov.saveql.sql.CompiledSql;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateDslTest {
    @Test
    void compilesUpdateByIdSql() {
        UUID id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        CompiledSql compiled = DSL.update(TestSchema.USERS)
                .set(TestSchema.USERS.name, "Updated")
                .byId(id)
                .compile();

        assertEquals("update users set name = ? where users.id = ?", compiled.sql());
        assertEquals(List.of("Updated", id), compiled.params());
    }

    @Test
    void compilesBulkUpdateSql() {
        TestSchema.OrdersTable o = TestSchema.ORDERS;
        CompiledSql compiled = DSL.update(o)
                .set(o.price, new BigDecimal("99.99"))
                .where(o.price.lt(new BigDecimal("10.00")))
                .compile();

        assertEquals("update orders set price = ? where orders.price < ?", compiled.sql());
        assertEquals(List.of(new BigDecimal("99.99"), new BigDecimal("10.00")), compiled.params());
    }

    @Test
    void rejectsUpdateByIdWithoutAssignments() {
        QueryValidationException ex = assertThrows(QueryValidationException.class, () ->
                DSL.update(TestSchema.USERS)
                        .byId(Values.of(UUID.randomUUID()))
                        .validate()
        );

        assertEquals("Update for table users does not contain any assignments", ex.getMessage());
    }
}
