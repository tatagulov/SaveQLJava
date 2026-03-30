package io.tatagulov.saveql;

import io.tatagulov.saveql.api.DSL;
import io.tatagulov.saveql.expr.AggregateFunctionExpr;
import io.tatagulov.saveql.expr.FunctionExpr;
import io.tatagulov.saveql.query.Query;
import io.tatagulov.saveql.query.QueryValidationException;
import io.tatagulov.saveql.schema.*;
import io.tatagulov.saveql.sql.CompiledMappedSql;
import io.tatagulov.saveql.sql.CompiledSql;
import io.tatagulov.saveql.sql.SqlCompiler;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

import static io.tatagulov.saveql.api.DSL.count;
import static io.tatagulov.saveql.api.DSL.sum;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DSLDslTest {

    @Test
    void buildsSqlWithoutAliasByDefault() {
        var users = TestSchema.USERS;

        Query<TestSchema.UsersTable> query = DSL.from(users)
                .where(users.name.eq("Ann"))
                .limit(10)
                .offset(5)
                .select(users.id, users.name)
                .build();

        CompiledSql compiled = new SqlCompiler().compile(query);

        assertEquals(
                "SELECT users.id, users.name FROM users WHERE users.name = ? LIMIT ? OFFSET ?",
                compiled.sql()
        );
        assertEquals(List.of("Ann", 10, 5), compiled.params());
    }

    @Test
    void buildsSqlWithExplicitAliasesOnly() {
        var orders = TestSchema.ORDERS.as("o");
        var users = TestSchema.USERS.as("u");

        Query<TestSchema.OrdersTable> query = DSL.from(orders)
                .join(users).by(orders.userId)
                .where(orders.price.gt(new BigDecimal("100.00")))
                .orderBy(sum(orders.price).desc())
                .limit(20)
                .select(
                        orders.id,
                        users.name,
                        sum(orders.price).as("totalPrice"),
                        count(orders.id)
                )
                .build();

        CompiledSql compiled = new SqlCompiler().compile(query);

        assertEquals(
                "SELECT o.id, u.name, sum(o.price) AS totalPrice, count(o.id) FROM orders o " +
                "JOIN users u ON o.user_id = u.id " +
                "WHERE o.price > ? GROUP BY o.id, u.name ORDER BY sum(o.price) DESC LIMIT ?",
                compiled.sql()
        );
        assertEquals(List.of(new BigDecimal("100.00"), 20), compiled.params());
    }

    @Test
    void buildsNestAggAndUsesFieldsInSelectAndWhere() {
        class OrdersByUser extends AggViewDef1<OrdersByUser, TestSchema.OrdersTable, UUID> {
            private static final TestSchema.OrdersTable orders = TestSchema.ORDERS;

            public final ColumnDef<OrdersByUser, BigDecimal> totalPrice = col(DSL.sum(orders.price), "totalPrice", BigDecimal.class);

            public OrdersByUser(String alias) {
                super(alias, Cols.of(orders.userId));
            }
        }


        var users = TestSchema.USERS.as("u");
        var ordersByUser = new OrdersByUser("order_stats");

        Query<TestSchema.UsersTable> query = DSL.from(users)
                .join(ordersByUser).by(users.id)
                .where(ordersByUser.totalPrice.gt(new BigDecimal("100")))
                .select(users.id, ordersByUser.totalPrice)
                .build();

        CompiledSql compiled = new SqlCompiler().compile(query);

        assertEquals(
                "SELECT u.id, order_stats.totalPrice FROM users u " +
                "JOIN (SELECT orders.user_id AS user_id, sum(orders.price) AS totalPrice " +
                "FROM orders GROUP BY orders.user_id) order_stats ON u.id = order_stats.user_id " +
                "WHERE order_stats.totalPrice > ?",
                compiled.sql()
        );
        assertEquals(List.of(new BigDecimal("100")), compiled.params());
    }

    @Test
    void movesAggregatePredicatesToHaving() {
        var orders = TestSchema.ORDERS;

        Query<TestSchema.OrdersTable> query = DSL.from(orders)
                .where(sum(orders.price).gt(new BigDecimal("1000")))
                .select(orders.userId, sum(orders.price))
                .build();

        CompiledSql compiled = new SqlCompiler().compile(query);

        assertEquals(
                "SELECT orders.user_id, sum(orders.price) FROM orders GROUP BY orders.user_id HAVING sum(orders.price) > ?",
                compiled.sql()
        );
        assertEquals(List.of(new BigDecimal("1000")), compiled.params());
    }

    @Test
    void movesAggregatePredicateToHavingWithoutAggregateInSelect() {
        var orders = TestSchema.ORDERS;

        Query<TestSchema.OrdersTable> query = DSL.from(orders)
                .where(sum(orders.price).gt(new BigDecimal("1000")))
                .select(orders.userId)
                .build();

        CompiledSql compiled = new SqlCompiler().compile(query);

        assertEquals(
                "SELECT orders.user_id FROM orders GROUP BY orders.user_id HAVING sum(orders.price) > ?",
                compiled.sql()
        );
        assertEquals(List.of(new BigDecimal("1000")), compiled.params());
    }

    @Test
    void treatsCustomAggregateFunctionAsAggregate() {
        var orders = TestSchema.ORDERS;
        var medianPrice = new AggregateFunctionExpr<>("median", BigDecimal.class, orders.price);

        Query<TestSchema.OrdersTable> query = DSL.from(orders)
                .where(medianPrice.gt(new BigDecimal("100")))
                .select(orders.userId, medianPrice)
                .build();

        CompiledSql compiled = new SqlCompiler().compile(query);

        assertEquals(
                "SELECT orders.user_id, median(orders.price) FROM orders GROUP BY orders.user_id " +
                "HAVING median(orders.price) > ?",
                compiled.sql()
        );
        assertEquals(List.of(new BigDecimal("100")), compiled.params());
    }

    @Test
    void splitsWhereAndHavingForAndPredicate() {
        var orders = TestSchema.ORDERS;

        Query<TestSchema.OrdersTable> query = DSL.from(orders)
                .where(
                        orders.price.gt(new BigDecimal("10"))
                                .and(sum(orders.price).gt(new BigDecimal("1000")))
                )
                .limit(5)
                .select(orders.userId, sum(orders.price))
                .build();

        CompiledSql compiled = new SqlCompiler().compile(query);

        assertEquals(
                "SELECT orders.user_id, sum(orders.price) FROM orders WHERE orders.price > ? GROUP BY orders.user_id " +
                "HAVING sum(orders.price) > ? LIMIT ?",
                compiled.sql()
        );
        assertEquals(List.of(new BigDecimal("10"), new BigDecimal("1000"), 5), compiled.params());
    }

    @Test
    void rejectsBlankAggregateViewAlias() {
        assertThrows(QueryValidationException.class, () -> new AggViewDef<>(
                " ",
                Cols.of(TestSchema.ORDERS.userId)
        ) {
        });
    }

    @Test
    void rejectsDuplicateQualifiersWhenJoiningTables() {
        var users = TestSchema.USERS.as("dup");
        var orders = TestSchema.ORDERS.as("dup");

        assertThrows(QueryValidationException.class, () -> DSL.from(users)
                .join(orders).by(Cols.of(users.id))
                .select(users.id)
                .build());
    }

    @Test
    void rejectsDuplicateQualifiersWhenJoiningAggregateView() {
        class OrdersByUser extends AggViewDef1<OrdersByUser, TestSchema.OrdersTable, UUID> {
            public OrdersByUser(String alias) {
                super(alias, Cols.of(TestSchema.ORDERS.userId));
            }
        }

        var users = TestSchema.USERS.as("same_alias");
        var view = new OrdersByUser("same_alias");

        assertThrows(QueryValidationException.class, () -> DSL.from(users)
                .join(view).by(Cols.of(users.id))
                .select(users.id)
                .build());
    }

    @Test
    void buildsAggregateViewJoinWithCompositeKey() {
        class MembershipTable extends TableDef2<MembershipTable, Long, Long> {
            public final ColumnDef<MembershipTable, Long> tenantId = col("tenant_id", Long.class);
            public final ColumnDef<MembershipTable, Long> userId = col("user_id", Long.class);
            private final Cols2<MembershipTable, Long, Long> primaryKey = Cols.of(tenantId, userId);

            public MembershipTable() {
                this(null);
            }

            private MembershipTable(String alias) {
                super("membership", alias);
            }

            @Override
            public Cols2<MembershipTable, Long, Long> primaryKeyCols() {
                return primaryKey;
            }

            @Override
            public MembershipTable as(String alias) {
                return new MembershipTable(alias);
            }
        }

        class PaymentsTable extends TableDef2<PaymentsTable, Long, Long> {
            public final ColumnDef<PaymentsTable, Long> tenantId = col("tenant_id", Long.class);
            public final ColumnDef<PaymentsTable, Long> userId = col("user_id", Long.class);
            public final ColumnDef<PaymentsTable, BigDecimal> amount = col("amount", BigDecimal.class);
            private final Cols2<PaymentsTable, Long, Long> primaryKey = Cols.of(tenantId, userId);

            public PaymentsTable() {
                this(null);
            }

            private PaymentsTable(String alias) {
                super("payments", alias);
            }

            @Override
            public Cols2<PaymentsTable, Long, Long> primaryKeyCols() {
                return primaryKey;
            }

            @Override
            public PaymentsTable as(String alias) {
                return new PaymentsTable(alias);
            }
        }

        PaymentsTable payments = new PaymentsTable();

        class PaymentsByUser extends AggViewDef2<PaymentsByUser, PaymentsTable, Long, Long> {
            public final ColumnDef<PaymentsByUser, BigDecimal> total = col(DSL.sum(payments.amount), "total", BigDecimal.class);

            public PaymentsByUser(String alias) {
                super(alias, Cols.of(payments.tenantId, payments.userId));
            }
        }

        var membership = new MembershipTable().as("m");
        var paymentStats = new PaymentsByUser("ps");

        Query<MembershipTable> query = DSL.from(membership)
                .join(paymentStats).by(membership.tenantId, membership.userId)
                .where(paymentStats.total.gt(new BigDecimal("100")))
                .select(membership.userId, paymentStats.total)
                .build();

        CompiledSql compiled = new SqlCompiler().compile(query);

        assertEquals(
                "SELECT m.user_id, ps.total FROM membership m " +
                "JOIN (SELECT payments.tenant_id AS tenant_id, payments.user_id AS user_id, sum(payments.amount) AS total " +
                "FROM payments GROUP BY payments.tenant_id, payments.user_id) ps " +
                "ON m.tenant_id = ps.tenant_id AND m.user_id = ps.user_id " +
                "WHERE ps.total > ?",
                compiled.sql()
        );
        assertEquals(List.of(new BigDecimal("100")), compiled.params());
    }

    @Test
    void buildsCompositeKeyJoinWithShorthandByColumns() {
        class MembershipTable extends TableDef2<MembershipTable, Long, Long> {
            public final ColumnDef<MembershipTable, Long> tenantId = col("tenant_id", Long.class);
            public final ColumnDef<MembershipTable, Long> userId = col("user_id", Long.class);
            private final Cols2<MembershipTable, Long, Long> primaryKey = Cols.of(tenantId, userId);

            public MembershipTable() {
                this(null);
            }

            private MembershipTable(String alias) {
                super("membership", alias);
            }

            @Override
            public Cols2<MembershipTable, Long, Long> primaryKeyCols() {
                return primaryKey;
            }

            @Override
            public MembershipTable as(String alias) {
                return new MembershipTable(alias);
            }
        }

        class PaymentsTable extends TableDef2<PaymentsTable, Long, Long> {
            public final ColumnDef<PaymentsTable, Long> tenantId = col("tenant_id", Long.class);
            public final ColumnDef<PaymentsTable, Long> userId = col("user_id", Long.class);
            public final ColumnDef<PaymentsTable, BigDecimal> amount = col("amount", BigDecimal.class);
            private final Cols2<PaymentsTable, Long, Long> primaryKey = Cols.of(tenantId, userId);

            public PaymentsTable() {
                this(null);
            }

            private PaymentsTable(String alias) {
                super("payments", alias);
            }

            @Override
            public Cols2<PaymentsTable, Long, Long> primaryKeyCols() {
                return primaryKey;
            }

            @Override
            public PaymentsTable as(String alias) {
                return new PaymentsTable(alias);
            }
        }

        var membership = new MembershipTable().as("m");
        var payments = new PaymentsTable().as("p");

        Query<MembershipTable> query = DSL.from(membership)
                .join(payments).by(membership.tenantId, membership.userId)
                .select(membership.userId, payments.amount)
                .build();

        CompiledSql compiled = new SqlCompiler().compile(query);

        assertEquals(
                "SELECT m.user_id, p.amount FROM membership m JOIN payments p ON m.tenant_id = p.tenant_id AND m.user_id = p.user_id",
                compiled.sql()
        );
        assertEquals(List.of(), compiled.params());
    }

    @Test
    void allowsWrappedAggregateExpressionInAggregateViewProjection() {
        class OrdersByUser extends AggViewDef1<OrdersByUser, TestSchema.OrdersTable, UUID> {
            private static final TestSchema.OrdersTable orders = TestSchema.ORDERS;
            public final ColumnDef<OrdersByUser, BigDecimal> absTotal =
                    col(new FunctionExpr<>("abs", BigDecimal.class, DSL.sum(orders.price)), "abs_total", BigDecimal.class);

            public OrdersByUser(String alias) {
                super(alias, Cols.of(orders.userId));
            }
        }

        var users = TestSchema.USERS.as("u");
        var view = new OrdersByUser("stats");

        Query<TestSchema.UsersTable> query = DSL.from(users)
                .join(view).by(users.id)
                .where(view.absTotal.gt(new BigDecimal("10")))
                .select(view.absTotal)
                .build();

        CompiledSql compiled = new SqlCompiler().compile(query);

        assertEquals(
                "SELECT stats.abs_total FROM users u " +
                "JOIN (SELECT orders.user_id AS user_id, abs(sum(orders.price)) AS abs_total " +
                "FROM orders GROUP BY orders.user_id) stats ON u.id = stats.user_id " +
                "WHERE stats.abs_total > ?",
                compiled.sql()
        );
        assertEquals(List.of(new BigDecimal("10")), compiled.params());
    }

    @Test
    void rejectsNonAggregateExpressionInAggregateViewProjection() {
        class InvalidView extends AggViewDef1<InvalidView, TestSchema.OrdersTable, UUID> {
            public InvalidView(String alias) {
                super(alias, Cols.of(TestSchema.ORDERS.userId));
                col(TestSchema.ORDERS.price, "plain_price", BigDecimal.class);
            }
        }

        assertThrows(QueryValidationException.class, () -> new InvalidView("invalid_view"));
    }

    @Test
    void rendersBasicArithmeticExpressions() {
        var orders = TestSchema.ORDERS;

        Query<TestSchema.OrdersTable> query = DSL.from(orders)
                .where(DSL.mod(orders.price, new BigDecimal("10")).gt(new BigDecimal("0")))
                .select(
                        DSL.add(orders.price, new BigDecimal("10")).as("plus_ten"),
                        DSL.sub(orders.price, new BigDecimal("1")).as("minus_one"),
                        DSL.mul(orders.price, new BigDecimal("2")).as("double_price"),
                        DSL.div(orders.price, new BigDecimal("2")).as("half_price")
                )
                .build();

        CompiledSql compiled = new SqlCompiler().compile(query);

        assertEquals(
                "SELECT (orders.price + ?) AS plus_ten, (orders.price - ?) AS minus_one, " +
                "(orders.price * ?) AS double_price, (orders.price / ?) AS half_price " +
                "FROM orders WHERE (orders.price % ?) > ?",
                compiled.sql()
        );
        assertEquals(
                List.of(
                        new BigDecimal("10"),
                        new BigDecimal("1"),
                        new BigDecimal("2"),
                        new BigDecimal("2"),
                        new BigDecimal("10"),
                        new BigDecimal("0")
                ),
                compiled.params()
        );
    }


    @Test
    void buildsLeftJoin() {
        var orders = TestSchema.ORDERS.as("o");
        var users = TestSchema.USERS.as("u");

        Query<TestSchema.OrdersTable> query = DSL.from(orders)
                .leftJoin(users).by(orders.userId)
                .where(users.name.isNull().or(users.name.eq("Ann")))
                .select(orders.id, users.name)
                .build();

        CompiledSql compiled = new SqlCompiler().compile(query);

        assertEquals(
                "SELECT o.id, u.name FROM orders o LEFT JOIN users u ON o.user_id = u.id WHERE u.name IS NULL or u.name = ?",
                compiled.sql()
        );
        assertEquals(List.of("Ann"), compiled.params());
    }

    @Test
    void rendersExtendedPredicates() {
        var users = TestSchema.USERS;
        var id1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var id2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

        Query<TestSchema.UsersTable> query = DSL.from(users)
                .where(
                        users.name.like("A%")
                                .and(users.name.ilike("%nn%"))
                                .and(users.name.ne("Bob"))
                                .and(users.name.isNotNull())
                                .and(users.id.in(List.of(id1, id2)))
                )
                .select(users.id, users.name)
                .build();

        CompiledSql compiled = new SqlCompiler().compile(query);

        assertEquals(
                "SELECT users.id, users.name FROM users WHERE users.name LIKE ? and users.name ILIKE ? and users.name <> ? and users.name IS NOT NULL and users.id IN (?, ?)",
                compiled.sql()
        );
        assertEquals(List.of("A%", "%nn%", "Bob", id1, id2), compiled.params());
    }

    @Test
    void rendersRangeComparisonsAndBetweenPredicates() {
        var orders = TestSchema.ORDERS;

        Query<TestSchema.OrdersTable> query = DSL.from(orders)
                .where(
                        orders.price.gte(new BigDecimal("10"))
                                .and(orders.price.lt(new BigDecimal("100")))
                                .and(orders.price.lte(new BigDecimal("99.99")))
                                .and(orders.price.between(new BigDecimal("11"), new BigDecimal("50")))
                                .and(orders.price.notBetween(new BigDecimal("60"), new BigDecimal("70")))
                )
                .select(orders.id)
                .build();

        CompiledSql compiled = new SqlCompiler().compile(query);

        assertEquals(
                "SELECT orders.id FROM orders WHERE orders.price >= ? and orders.price < ? and orders.price <= ? and orders.price BETWEEN ? AND ? and orders.price NOT BETWEEN ? AND ?",
                compiled.sql()
        );
        assertEquals(
                List.of(
                        new BigDecimal("10"),
                        new BigDecimal("100"),
                        new BigDecimal("99.99"),
                        new BigDecimal("11"),
                        new BigDecimal("50"),
                        new BigDecimal("60"),
                        new BigDecimal("70")
                ),
                compiled.params()
        );
    }

    @Test
    void supportsJoinJoinOnOnSyntax() {
        var orders = TestSchema.ORDERS.as("o");
        var users = TestSchema.USERS.as("u");
        var orders2 = TestSchema.ORDERS.as("o2");

        Query<TestSchema.OrdersTable> query = DSL.from(orders)
                .join(users)
                .join(orders2)
                .by(users.id)
                .by(orders.userId)
                .select(orders.id, users.name)
                .build();

        CompiledSql compiled = new SqlCompiler().compile(query);

        assertEquals(
                "SELECT o.id, u.name FROM orders o " +
                "JOIN users u ON o.user_id = u.id " +
                "JOIN orders o2 ON u.id = o2.id",
                compiled.sql()
        );
        assertEquals(List.of(), compiled.params());
    }

    @Test
    void rendersCaseWhenExpression() {
        var users = TestSchema.USERS;

        Query<TestSchema.UsersTable> query = DSL.from(users)
                .select(
                        users.id,
                        DSL.caseWhen(users.name.isNull(), "unknown")
                                .when(users.name.like("A%"), "starts_with_a")
                                .otherwise(users.name)
                                .as("name_bucket")
                )
                .build();

        CompiledSql compiled = new SqlCompiler().compile(query);

        assertEquals(
                "SELECT users.id, CASE WHEN users.name IS NULL THEN ? WHEN users.name LIKE ? THEN ? ELSE users.name END AS name_bucket FROM users",
                compiled.sql()
        );
        assertEquals(List.of("unknown", "A%", "starts_with_a"), compiled.params());
    }

    @Test
    void rendersOrderByWithNullsModifiers() {
        var users = TestSchema.USERS;

        Query<TestSchema.UsersTable> query = DSL.from(users)
                .orderBy(
                        users.name.asc().nullsLast(),
                        users.id.desc().nullsFirst()
                )
                .select(users.id, users.name)
                .build();

        CompiledSql compiled = new SqlCompiler().compile(query);

        assertEquals(
                "SELECT users.id, users.name FROM users ORDER BY users.name ASC NULLS LAST, users.id DESC NULLS FIRST",
                compiled.sql()
        );
        assertEquals(List.of(), compiled.params());
    }

    @Test
    void compilesProjectToWithTypedMappings() {
        var users = TestSchema.USERS;

        @Data
        class UserDto {
            private String userName;
            private UUID userId;
        }

        CompiledMappedSql<UserDto> compiled = DSL.from(users)
                .where(users.name.isNotNull())
                .selectTo(UserDto::new)
                .map(users.name, UserDto::setUserName)
                .map(users.id, UserDto::setUserId)
                .compile();

        assertEquals(
                "SELECT users.name, users.id FROM users WHERE users.name IS NOT NULL",
                compiled.sql()
        );
        assertEquals(List.of(), compiled.params());
    }

    @Test
    void mapsResultSetToDtoUsingCompiledMappedSql() throws Exception {
        var users = TestSchema.USERS;
        var id = UUID.fromString("33333333-3333-3333-3333-333333333333");

        @Data
        class UserDto {
            private String userName;
            private UUID userId;
        }

        CompiledMappedSql<UserDto> compiled = DSL.from(users)
                .selectTo(UserDto::new)
                .map(users.name, UserDto::setUserName)
                .map(users.id, UserDto::setUserId)
                .compile();

        ResultSet rs = mock(ResultSet.class);
        when(rs.getObject(1, String.class)).thenReturn("Ann");
        when(rs.getObject(2, UUID.class)).thenReturn(id);

        UserDto dto = compiled.rowMapper().map(rs);
        assertEquals("Ann", dto.userName);
        assertEquals(id, dto.userId);
    }

    @Test
    void buildsSelectCountAtPipelineEnd() {
        var users = TestSchema.USERS;

        Query<TestSchema.UsersTable> query = DSL.from(users)
                .where(users.name.isNotNull())
                .selectCount()
                .build();

        CompiledSql compiled = new SqlCompiler().compile(query);

        assertEquals(
                "SELECT count(users.id) FROM users WHERE users.name IS NOT NULL",
                compiled.sql()
        );
        assertEquals(List.of(), compiled.params());
    }

}
