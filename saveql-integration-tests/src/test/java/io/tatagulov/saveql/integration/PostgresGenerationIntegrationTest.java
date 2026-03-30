package io.tatagulov.saveql.integration;

import io.tatagulov.saveql.api.DSL;
import io.tatagulov.saveql.api.InsertRow;
import io.tatagulov.saveql.expr.AggregateFunctionExpr;
import io.tatagulov.saveql.generated.pg.*;
import io.tatagulov.saveql.query.QueryValidationException;
import io.tatagulov.saveql.query.Query;
import io.tatagulov.saveql.schema.Cols;
import io.tatagulov.saveql.sql.CompiledSql;
import io.tatagulov.saveql.sql.SqlCompiler;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PostgresGenerationIntegrationTest {
    private static final String JDBC_URL = System.getProperty("saveql.it.jdbc.url");

    @Data
    private static final class UserSummaryDto {
        private UUID id;
        private String displayName;
        private Long score;
        private BigDecimal balance;
    }

    @Test
    void generatesClassesViaMavenPluginAndExecutesDslAgainstRealPostgres() throws Exception {
        assertNotNull(PostgresSchema.APP_USER);
        assertNotNull(PostgresSchema.PURCHASE_ORDER);
        assertNotNull(PostgresSchema.MEMBERSHIP);
        assertNotNull(PostgresSchema.ALL_TYPE_SAMPLES);

        assertEquals(2, primaryKeySize(PostgresSchema.MEMBERSHIP));
        assertNotNull(PostgresSchema.ALL_TYPE_SAMPLES.uuidValue);
        assertNotNull(PostgresSchema.ALL_TYPE_SAMPLES.offsetTimestampValue);
        assertNotNull(PostgresSchema.ALL_TYPE_SAMPLES.bytesValue);
        assertTrue(PostgresSchema.PURCHASE_ORDER.id.isGenerated());
        assertFalse(PostgresSchema.APP_USER.displayName.isNullable());
        assertFalse(PostgresSchema.APP_USER.createdAt.hasDefaultValue());
        assertEquals(OffsetDateTime.class, PostgresSchema.APP_USER.updatedAt.getType());
        assertEquals(OffsetDateTime.class, PostgresSchema.ALL_TYPE_SAMPLES.offsetTimestampValue.getType());

        Query<AppUserTable> usersQuery = DSL.from(PostgresSchema.APP_USER)
                .where(PostgresSchema.APP_USER.active.eq(true).and(PostgresSchema.APP_USER.score.gt(100L)))
                .select(PostgresSchema.APP_USER.id, PostgresSchema.APP_USER.displayName)
                .build();

        CompiledSql usersSql = new SqlCompiler().compile(usersQuery);
        assertEquals(
                "SELECT app_user.id, app_user.display_name FROM app_user WHERE app_user.active = ? and app_user.score > ?",
                usersSql.sql()
        );
        assertEquals(List.of(true, 100L), usersSql.params());

        try (Connection connection = DriverManager.getConnection(JDBC_URL)) {
            seedReferenceData(connection);

            List<List<Object>> userRows = execute(connection, usersSql);
            assertEquals(1, userRows.size());
            assertEquals(UUID.fromString("11111111-1111-1111-1111-111111111111"), userRows.get(0).get(0));
            assertEquals("Ann", userRows.get(0).get(1));

            PurchaseOrderTable orderAlias = PostgresSchema.PURCHASE_ORDER.as("o");
            AppUserTable userAlias = PostgresSchema.APP_USER.as("u");
            AggregateFunctionExpr<BigDecimal> totalAmount = DSL.sum(orderAlias.amount);

            Query<PurchaseOrderTable> aggregateQuery = DSL.from(orderAlias)
                    .join(userAlias).by(Cols.of(orderAlias.userId))
                    .where(orderAlias.amount.gt(new BigDecimal("30.00")))
                    .select(userAlias.displayName, totalAmount.as("total_amount"))
                    .build();

            CompiledSql aggregateSql = new SqlCompiler().compile(aggregateQuery);
            List<List<Object>> aggregateRows = execute(connection, aggregateSql);
            assertEquals(1, aggregateRows.size());
            assertEquals("Ann", aggregateRows.get(0).get(0));
            assertEquals(new BigDecimal("150.00"), aggregateRows.get(0).get(1));

            Query<AppUserTable> payloadQuery = DSL.from(PostgresSchema.APP_USER)
                    .where(PostgresSchema.APP_USER.displayName.eq("Ann"))
                    .select(PostgresSchema.APP_USER.payload)
                    .build();
            List<List<Object>> payloadRows = execute(connection, new SqlCompiler().compile(payloadQuery));
            assertEquals(1, payloadRows.size());
            assertArrayEquals(new byte[]{(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF}, (byte[]) payloadRows.get(0).get(0));
        }
    }

    @Test
    void executesInsertAgainstRealPostgresAndValidatesRequiredAndGeneratedColumns() throws Exception {
        UUID id = UUID.fromString("44444444-4444-4444-4444-444444444444");

        try (Connection connection = DriverManager.getConnection(JDBC_URL)) {
            int inserted = DSL.insertInto(PostgresSchema.APP_USER)
                    .set(PostgresSchema.APP_USER.id, id)
                    .set(PostgresSchema.APP_USER.displayName, "Cara")
                    .set(PostgresSchema.APP_USER.email, "cara@example.com")
                    .set(PostgresSchema.APP_USER.active, true)
                    .set(PostgresSchema.APP_USER.score, 150L)
                    .set(PostgresSchema.APP_USER.balance, new BigDecimal("700.25"))
                    .set(PostgresSchema.APP_USER.createdAt, LocalDateTime.of(2025, 3, 10, 9, 0))
                    .set(PostgresSchema.APP_USER.updatedAt, OffsetDateTime.of(2025, 3, 10, 9, 0, 0, 0, ZoneOffset.ofHours(5)))
                    .execute(connection);

            assertEquals(1, inserted);
            List<List<Object>> insertedRows = execute(connection, new SqlCompiler().compile(
                    DSL.from(PostgresSchema.APP_USER)
                            .where(PostgresSchema.APP_USER.id.eq(id))
                            .select(
                                    PostgresSchema.APP_USER.displayName,
                                    PostgresSchema.APP_USER.email,
                                    PostgresSchema.APP_USER.balance
                            )
                            .build()
            ));
            assertEquals(1, insertedRows.size());
            assertEquals("Cara", insertedRows.get(0).get(0));
            assertEquals("cara@example.com", insertedRows.get(0).get(1));
            assertEquals(new BigDecimal("700.25"), insertedRows.get(0).get(2));
        }

        QueryValidationException missingRequired = assertThrows(QueryValidationException.class, () ->
                DSL.insertInto(PostgresSchema.APP_USER)
                        .set(PostgresSchema.APP_USER.id, UUID.randomUUID())
                        .set(PostgresSchema.APP_USER.displayName, "Missing")
                        .validate()
        );
        assertEquals("Required column email is missing for table app_user", missingRequired.getMessage());

        QueryValidationException generatedColumn = assertThrows(QueryValidationException.class, () ->
                DSL.insertInto(PostgresSchema.PURCHASE_ORDER)
                        .set(PostgresSchema.PURCHASE_ORDER.id, 1L)
        );
        assertEquals("Column id is generated and cannot be assigned explicitly", generatedColumn.getMessage());
    }

    @Test
    void executesBatchInsertAgainstRealPostgres() throws Exception {
        UUID firstId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        UUID secondId = UUID.fromString("66666666-6666-6666-6666-666666666666");

        try (Connection connection = DriverManager.getConnection(JDBC_URL)) {
            int[] inserted = DSL.insertInto(PostgresSchema.APP_USER)
                    .batch(List.of(
                            InsertRow.into(PostgresSchema.APP_USER)
                                    .set(PostgresSchema.APP_USER.id, firstId)
                                    .set(PostgresSchema.APP_USER.displayName, "Dina")
                                    .set(PostgresSchema.APP_USER.email, "dina@example.com")
                                    .set(PostgresSchema.APP_USER.active, true)
                                    .set(PostgresSchema.APP_USER.score, 210L)
                                    .set(PostgresSchema.APP_USER.balance, new BigDecimal("810.00"))
                                    .set(PostgresSchema.APP_USER.createdAt, LocalDateTime.of(2025, 3, 11, 10, 0))
                                    .set(PostgresSchema.APP_USER.updatedAt, OffsetDateTime.of(2025, 3, 11, 10, 0, 0, 0, ZoneOffset.ofHours(5))),
                            InsertRow.into(PostgresSchema.APP_USER)
                                    .set(PostgresSchema.APP_USER.id, secondId)
                                    .set(PostgresSchema.APP_USER.displayName, "Evan")
                                    .set(PostgresSchema.APP_USER.email, "evan@example.com")
                                    .set(PostgresSchema.APP_USER.active, false)
                                    .set(PostgresSchema.APP_USER.score, 90L)
                                    .set(PostgresSchema.APP_USER.balance, new BigDecimal("120.50"))
                                    .set(PostgresSchema.APP_USER.createdAt, LocalDateTime.of(2025, 3, 11, 11, 0))
                                    .set(PostgresSchema.APP_USER.updatedAt, OffsetDateTime.of(2025, 3, 11, 11, 0, 0, 0, ZoneOffset.ofHours(5)))
                    ))
                    .batchSize(1)
                    .execute(connection);

            assertArrayEquals(new int[]{1, 1}, inserted);

            List<List<Object>> rows = execute(connection, new SqlCompiler().compile(
                    DSL.from(PostgresSchema.APP_USER)
                            .where(
                                    PostgresSchema.APP_USER.id.eq(firstId)
                                            .or(PostgresSchema.APP_USER.id.eq(secondId))
                            )
                            .select(PostgresSchema.APP_USER.id, PostgresSchema.APP_USER.displayName)
                            .build()
            ));

            assertEquals(2, rows.size());
        }
    }

    @Test
    void selectsDirectlyIntoDto() throws Exception {
        try (Connection connection = DriverManager.getConnection(JDBC_URL)) {
            seedReferenceData(connection);

            AppUserTable u = PostgresSchema.APP_USER;
            List<UserSummaryDto> rows = DSL.from(u)
                    .where(u.score.gt(70L))
                    .orderBy(u.displayName.asc())
                    .selectTo(UserSummaryDto::new)
                    .map(u.id, UserSummaryDto::setId)
                    .map(u.displayName, UserSummaryDto::setDisplayName)
                    .map(u.score, UserSummaryDto::setScore)
                    .map(u.balance, UserSummaryDto::setBalance)
                    .fetch(connection);

            assertEquals(2, rows.size());

            UserSummaryDto first = rows.get(0);
            assertEquals(UUID.fromString("11111111-1111-1111-1111-111111111111"), first.id);
            assertEquals("Ann", first.displayName);
            assertEquals(120L, first.score);
            assertEquals(new BigDecimal("1500.50"), first.balance);

            UserSummaryDto second = rows.get(1);
            assertEquals(UUID.fromString("22222222-2222-2222-2222-222222222222"), second.id);
            assertEquals("Bob", second.displayName);
            assertEquals(80L, second.score);
            assertEquals(new BigDecimal("300.00"), second.balance);
        }
    }

    @Test
    void updatesSingleRowByPrimaryKeyAndBulkRowsByWhere() throws Exception {
        try (Connection connection = DriverManager.getConnection(JDBC_URL)) {
            seedReferenceData(connection);

            int updatedById = DSL.update(PostgresSchema.APP_USER)
                    .set(PostgresSchema.APP_USER.displayName, "Ann Updated")
                    .set(PostgresSchema.APP_USER.balance, new BigDecimal("1600.00"))
                    .byId(io.tatagulov.saveql.schema.Values.of(UUID.fromString("11111111-1111-1111-1111-111111111111")))
                    .execute(connection);
            assertEquals(1, updatedById);

            int bulkUpdated = DSL.update(PostgresSchema.PURCHASE_ORDER)
                    .set(PostgresSchema.PURCHASE_ORDER.status, "ARCHIVED")
                    .where(PostgresSchema.PURCHASE_ORDER.amount.lt(new BigDecimal("50.00")))
                    .execute(connection);
            assertEquals(2, bulkUpdated);

            List<UserSummaryDto> users = DSL.from(PostgresSchema.APP_USER)
                    .where(PostgresSchema.APP_USER.id.eq(UUID.fromString("11111111-1111-1111-1111-111111111111")))
                    .selectTo(UserSummaryDto::new)
                    .map(PostgresSchema.APP_USER.id, (dto, value) -> dto.id = value)
                    .map(PostgresSchema.APP_USER.displayName, (dto, value) -> dto.displayName = value)
                    .map(PostgresSchema.APP_USER.score, (dto, value) -> dto.score = value)
                    .map(PostgresSchema.APP_USER.balance, (dto, value) -> dto.balance = value)
                    .fetch(connection);
            assertEquals(1, users.size());
            assertEquals("Ann Updated", users.get(0).displayName);
            assertEquals(new BigDecimal("1600.00"), users.get(0).balance);

            List<List<Object>> archivedOrders = execute(connection, new SqlCompiler().compile(
                    DSL.from(PostgresSchema.PURCHASE_ORDER)
                            .where(PostgresSchema.PURCHASE_ORDER.status.eq("ARCHIVED"))
                            .select(PostgresSchema.PURCHASE_ORDER.amount)
                            .build()
            ));
            assertEquals(2, archivedOrders.size());
        }
    }

    private static int primaryKeySize(io.tatagulov.saveql.schema.TableDef<?, ?> table) {
        List<Object> columns = new ArrayList<>();
        table.primaryKeyCols().forEach(columns::add);
        return columns.size();
    }

    private void seedReferenceData(Connection connection) throws Exception {
        UUID annId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID bobId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        AppUserTable u = PostgresSchema.APP_USER;
        int[] insertedUsers = DSL.insertInto(u)
                .batch(List.of(
                        DSL.insertRow(u)
                                .set(u.id, annId)
                                .set(u.displayName, "Ann")
                                .set(u.email, "ann@example.com")
                                .set(u.active, true)
                                .set(u.age, 31)
                                .set(u.score, 120L)
                                .set(u.balance, new BigDecimal("1500.50"))
                                .set(u.rating, 4.5f)
                                .set(u.reputation, 98.2d)
                                .set(u.birthDate, LocalDate.of(1994, 5, 1))
                                .set(u.preferredContactTime, OffsetTime.of(9, 15, 0, 0, ZoneOffset.ofHours(5)))
                                .set(u.createdAt, LocalDateTime.of(2025, 1, 10, 8, 30))
                                .set(u.updatedAt, OffsetDateTime.of(2025, 1, 10, 8, 30, 0, 0, ZoneOffset.ofHours(5)))
                                .set(u.payload, new byte[]{(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF}),
                        DSL.insertRow(u)
                                .set(u.id, bobId)
                                .set(u.displayName, "Bob")
                                .set(u.email, "bob@example.com")
                                .set(u.active, false)
                                .set(u.age, 28)
                                .set(u.score, 80L)
                                .set(u.balance, new BigDecimal("300.00"))
                                .set(u.rating, 3.8f)
                                .set(u.reputation, 64.4d)
                                .set(u.birthDate, LocalDate.of(1997, 9, 17))
                                .set(u.preferredContactTime, OffsetTime.of(18, 40, 0, 0, ZoneOffset.ofHours(5)))
                                .set(u.createdAt, LocalDateTime.of(2025, 1, 11, 11, 0))
                                .set(u.updatedAt, OffsetDateTime.of(2025, 1, 11, 11, 0, 0, 0, ZoneOffset.ofHours(5)))
                                .set(u.payload, new byte[]{(byte) 0xBA, (byte) 0xDD, (byte) 0xCA, (byte) 0xFE})
                ))
                .execute(connection);
        assertArrayEquals(new int[]{1, 1}, insertedUsers);

        PurchaseOrderTable po = PostgresSchema.PURCHASE_ORDER;
        int[] insertedOrders = DSL.insertInto(po)
                .batch(List.of(
                        DSL.insertRow(po)
                                .set(po.userId, annId)
                                .set(po.amount, new BigDecimal("100.25"))
                                .set(po.status, "PAID")
                                .set(po.createdAt, OffsetDateTime.of(2025, 2, 1, 10, 0, 0, 0, ZoneOffset.ofHours(5))),
                        DSL.insertRow(po)
                                .set(po.userId, annId)
                                .set(po.amount, new BigDecimal("49.75"))
                                .set(po.status, "PAID")
                                .set(po.createdAt, OffsetDateTime.of(2025, 2, 2, 11, 30, 0, 0, ZoneOffset.ofHours(5))),
                        DSL.insertRow(po)
                                .set(po.userId, bobId)
                                .set(po.amount, new BigDecimal("20.00"))
                                .set(po.status, "NEW")
                                .set(po.createdAt, OffsetDateTime.of(2025, 2, 3, 9, 0, 0, 0, ZoneOffset.ofHours(5)))
                ))
                .execute(connection);
        assertArrayEquals(new int[]{1, 1, 1}, insertedOrders);

        MembershipTable m = PostgresSchema.MEMBERSHIP;
        int[] insertedMemberships = DSL.insertInto(m)
                .batch(List.of(
                        DSL.insertRow(m)
                                .set(m.tenantId, 10L)
                                .set(m.userId, annId)
                                .set(m.roleName, "ADMIN")
                                .set(m.active, true),
                        DSL.insertRow(m)
                                .set(m.tenantId, 10L)
                                .set(m.userId, bobId)
                                .set(m.roleName, "VIEWER")
                                .set(m.active, true)
                ))
                .execute(connection);
        assertArrayEquals(new int[]{1, 1}, insertedMemberships);

        AllTypeSamplesTable ats = PostgresSchema.ALL_TYPE_SAMPLES;
        int insertedSample = DSL.insertInto(ats)
                .set(ats.uuidValue, UUID.fromString("33333333-3333-3333-3333-333333333333"))
                .set(ats.shortText, "hello")
                .set(ats.longText, "long text value")
                .set(ats.boolValue, true)
                .set(ats.intValue, 42)
                .set(ats.longValue, 42000000000L)
                .set(ats.decimalValue, new BigDecimal("1234.5678"))
                .set(ats.floatValue, 12.5f)
                .set(ats.doubleValue, 45.125d)
                .set(ats.localDateValue, LocalDate.of(2025, 3, 1))
                .set(ats.offsetTimeValue, OffsetTime.of(7, 45, 0, 0, ZoneOffset.ofHours(5)))
                .set(ats.localTimestampValue, LocalDateTime.of(2025, 3, 1, 7, 45))
                .set(ats.offsetTimestampValue, OffsetDateTime.of(2025, 3, 1, 7, 45, 0, 0, ZoneOffset.ofHours(5)))
                .set(ats.bytesValue, new byte[]{1, 2, 3, 4})
                .execute(connection);
        assertEquals(1, insertedSample);
    }

    private List<List<Object>> execute(Connection connection, CompiledSql compiledSql) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(compiledSql.sql())) {
            List<Object> params = compiledSql.params();
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = statement.executeQuery()) {
                List<List<Object>> rows = new ArrayList<>();
                int columns = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    List<Object> row = new ArrayList<>(columns);
                    for (int i = 1; i <= columns; i++) {
                        row.add(rs.getObject(i));
                    }
                    rows.add(row);
                }
                return rows;
            }
        }
    }
}
