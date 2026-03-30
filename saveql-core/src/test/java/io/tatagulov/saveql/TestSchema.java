package io.tatagulov.saveql;

import io.tatagulov.saveql.schema.*;

import java.math.BigDecimal;
import java.util.UUID;

public final class TestSchema {
    private TestSchema() {
    }

    public static final UsersTable USERS = new UsersTable();
    public static final OrdersTable ORDERS = new OrdersTable();
    public static final CategoriesTable CATEGORIES = new CategoriesTable();

    public static final class UsersTable extends TableDef1<UsersTable, UUID> {
        public final ColumnDef<UsersTable, UUID> id;
        public final ColumnDef<UsersTable, UUID> id2;
        public final ColumnDef<UsersTable, String> name;

        private final Cols1<UsersTable, UUID> primaryKey;

        public UsersTable() {
            this(null);
        }

        private UsersTable(String alias) {
            super("users", alias);
            id = col("id", UUID.class, false, false, false);
            id2 = col("id2", UUID.class, true, false, false);
            name = col("name", String.class, false, false, false);
            primaryKey = Cols.of(id);
        }


        @Override
        public Cols1<UsersTable, UUID> primaryKeyCols() {
            return primaryKey;
        }

        @Override
        public UsersTable as(String alias) {
            return new UsersTable(alias);
        }
    }

    public static final class OrdersTable extends TableDef1<OrdersTable, UUID> {
        public final ColumnDef<OrdersTable, UUID> id;
        public final ColumnDef<OrdersTable, UUID> userId;
        public final ColumnDef<OrdersTable, BigDecimal> price;


        private final Cols1<OrdersTable, UUID> primaryKey;

        public OrdersTable() {
            this(null);
        }

        private OrdersTable(String alias) {
            super("orders", alias);
            id = col("id", UUID.class, false, false, false);
            userId = col("user_id", UUID.class, false, false, false);
            price = col("price", BigDecimal.class, false, false, false);
            primaryKey = Cols.of(id);
        }

        @Override
        public Cols1<OrdersTable, UUID> primaryKeyCols() {
            return primaryKey;
        }

        @Override
        public OrdersTable as(String alias) {
            return new OrdersTable(alias);
        }
    }

    public static final class CategoriesTable extends TableDef1<CategoriesTable, Long> {
        public final ColumnDef<CategoriesTable, Long> id = col("id", Long.class, false, false, false);
        public final ColumnDef<CategoriesTable, Long> parentId = col("parent_id", Long.class, true, false, false);
        public final ColumnDef<CategoriesTable, Boolean> isArchived = col("is_archived", Boolean.class, false, true, false);
        public final ColumnDef<CategoriesTable, Boolean> isHidden = col("is_hidden", Boolean.class, true, false, false);

        private final Cols1<CategoriesTable, Long> primaryKey;

        public CategoriesTable() {
            this(null);
        }

        private CategoriesTable(String alias) {
            super("category", alias);
            primaryKey = Cols.of(id);
        }

        @Override
        public Cols1<CategoriesTable, Long> primaryKeyCols() {
            return primaryKey;
        }

        @Override
        public CategoriesTable as(String alias) {
            return new CategoriesTable(alias);
        }
    }
}
