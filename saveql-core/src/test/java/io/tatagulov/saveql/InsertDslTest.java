package io.tatagulov.saveql;

import io.tatagulov.saveql.api.DSL;
import io.tatagulov.saveql.api.InsertRow;
import io.tatagulov.saveql.query.QueryValidationException;
import io.tatagulov.saveql.schema.Cols;
import io.tatagulov.saveql.schema.Cols1;
import io.tatagulov.saveql.schema.ColumnDef;
import io.tatagulov.saveql.schema.TableDef1;
import io.tatagulov.saveql.sql.CompiledSql;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class InsertDslTest {
    @Test
    void buildsInsertSqlAndParametersInSetOrder() {
        UUID id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        CompiledSql compiled = DSL.insertInto(TestSchema.USERS)
                .set(TestSchema.USERS.id, id)
                .set(TestSchema.USERS.name, "Ann")
                .compile();

        assertEquals("insert into users (id, name) values (?, ?)", compiled.sql());
        assertEquals(List.of(id, "Ann"), compiled.params());
    }

    @Test
    void rejectsMissingRequiredColumn() {
        QueryValidationException ex = assertThrows(QueryValidationException.class, () ->
                DSL.insertInto(TestSchema.USERS)
                        .set(TestSchema.USERS.id, UUID.randomUUID())
                        .validate()
        );

        assertEquals("Required column name is missing for table users", ex.getMessage());
    }

    @Test
    void rejectsNullForRequiredColumn() {
        QueryValidationException ex = assertThrows(QueryValidationException.class, () ->
                DSL.insertInto(TestSchema.USERS)
                        .set(TestSchema.USERS.id, UUID.randomUUID())
                        .set(TestSchema.USERS.name, null)
                        .validate()
        );

        assertEquals("Required column name cannot be null for table users", ex.getMessage());
    }

    @Test
    void allowsOmittingNullableAndDefaultedColumns() {
        DSL.insertInto(TestSchema.CATEGORIES)
                .set(TestSchema.CATEGORIES.id, 1L)
                .validate();
    }

    @Test
    void rejectsGeneratedColumnAssignment() {
        class GeneratedTable extends TableDef1<GeneratedTable, Long> {
            final ColumnDef<GeneratedTable, Long> id;
            final ColumnDef<GeneratedTable, String> name;
            private final Cols1<GeneratedTable, Long> primaryKey;

            GeneratedTable() {
                super("generated_table", null);
                id = col("id", Long.class, false, false, true);
                name = col("name", String.class, false, false, false);
                primaryKey = Cols.of(id);
            }

            @Override
            public Cols1<GeneratedTable, Long> primaryKeyCols() {
                return primaryKey;
            }

            @Override
            public GeneratedTable as(String alias) {
                throw new UnsupportedOperationException();
            }
        }

        GeneratedTable table = new GeneratedTable();
        QueryValidationException ex = assertThrows(QueryValidationException.class, () ->
                DSL.insertInto(table).set(table.id, 1L)
        );

        assertEquals("Column id is generated and cannot be assigned explicitly", ex.getMessage());
    }

    @Test
    void buildsBatchInsertFromTypedRows() {
        UUID firstId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID secondId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);

        try {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeBatch()).thenReturn(new int[]{1}, new int[]{1});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        int[] result;
        try {
            TestSchema.UsersTable u = TestSchema.USERS;

            result = DSL.insertInto(u)
                .batch(List.of(
                        InsertRow.into(u)
                                .set(u.id, firstId)
                                .set(u.name, "Ann"),
                        InsertRow.into(u)
                                .set(u.name, "Bob")
                                .set(u.id, secondId)
                ))
                .batchSize(1)
                .execute(connection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertEquals(List.of(1, 1), java.util.Arrays.stream(result).boxed().toList());
        try {
            verify(connection).prepareStatement("insert into users (id, name) values (?, ?)");
            org.mockito.InOrder ordered = inOrder(statement);
            ordered.verify(statement).setObject(1, firstId);
            ordered.verify(statement).setObject(2, "Ann");
            ordered.verify(statement).addBatch();
            ordered.verify(statement).executeBatch();
            ordered.verify(statement).setObject(1, secondId);
            ordered.verify(statement).setObject(2, "Bob");
            ordered.verify(statement).addBatch();
            ordered.verify(statement).executeBatch();
            verify(statement).close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void fillsMissingOptionalBatchValuesWithNull() {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);

        try {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeBatch()).thenReturn(new int[]{1, 1});

            int[] result = DSL.insertInto(TestSchema.CATEGORIES)
                    .batch(List.of(
                            InsertRow.into(TestSchema.CATEGORIES)
                                    .set(TestSchema.CATEGORIES.id, 1L)
                                    .set(TestSchema.CATEGORIES.parentId, 10L),
                            InsertRow.into(TestSchema.CATEGORIES)
                                    .set(TestSchema.CATEGORIES.id, 2L)
                    ))
                    .execute(connection);

            assertEquals(List.of(1, 1), java.util.Arrays.stream(result).boxed().toList());

            verify(connection).prepareStatement("insert into category (id, parent_id) values (?, ?)");
            org.mockito.InOrder ordered = inOrder(statement);
            ordered.verify(statement).setObject(1, 1L);
            ordered.verify(statement).setObject(2, 10L);
            ordered.verify(statement).addBatch();
            ordered.verify(statement).setObject(1, 2L);
            ordered.verify(statement).setObject(2, null);
            ordered.verify(statement).addBatch();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void stillRejectsMissingRequiredColumnsInBatch() {
        QueryValidationException ex = assertThrows(QueryValidationException.class, () ->
                DSL.insertInto(TestSchema.USERS)
                        .batch(List.of(
                                InsertRow.into(TestSchema.USERS)
                                        .set(TestSchema.USERS.id, UUID.randomUUID())
                                        .set(TestSchema.USERS.name, "Ann"),
                                InsertRow.into(TestSchema.USERS)
                                        .set(TestSchema.USERS.id, UUID.randomUUID())
                        ))
                        .validate()
        );

        assertEquals("Required column name is missing for table users", ex.getMessage());
    }
}
