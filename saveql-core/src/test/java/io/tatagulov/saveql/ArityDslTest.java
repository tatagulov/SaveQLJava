package io.tatagulov.saveql;

import io.tatagulov.saveql.api.DSL;
import io.tatagulov.saveql.query.Query;
import io.tatagulov.saveql.schema.*;
import io.tatagulov.saveql.sql.CompiledSql;
import io.tatagulov.saveql.sql.SqlCompiler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArityDslTest {
    private static final TripleKeyTable TRIPLE_KEY = new TripleKeyTable("triple_key", null);
    private static final QuadKeyTable QUAD_KEY = new QuadKeyTable("quad_key", null);
    private static final FiveKeyTable FIVE_KEY = new FiveKeyTable("five_key_table", null);

    @Test
    void zipsThreeColumnsWithThreeValues() {
        List<String> collected = new ArrayList<>();

        TRIPLE_KEY.primaryKeyCols().zip(Values.of(10L, 20L, 30L), new ColumnValueConsumer<>() {
            @Override
            public <T> void each(ColumnLike<TripleKeyTable, T> leftColumn, T value) {
                collected.add(leftColumn.name() + "=" + value);
            }
        });

        assertEquals(List.of("p1=10", "p2=20", "p3=30"), collected);
    }

    @Test
    void zipsFourColumnsWithFourValues() {
        List<String> collected = new ArrayList<>();

        QUAD_KEY.primaryKeyCols().zip(Values.of(1L, 2L, 3L, 4L), new ColumnValueConsumer<>() {
            @Override
            public <T> void each(ColumnLike<QuadKeyTable, T> leftColumn, T value) {
                collected.add(leftColumn.name() + "=" + value);
            }
        });

        assertEquals(List.of("p1=1", "p2=2", "p3=3", "p4=4"), collected);
    }

    @Test
    void compilesUpdateByIdForFiveColumnPrimaryKey() {
        CompiledSql compiled = DSL.update(FIVE_KEY)
                .set(FIVE_KEY.payload, "updated")
                .byId(1L, 2L, 3L, 4L, 5L)
                .compile();

        assertEquals(
                "update five_key_table set payload = ? where five_key_table.p1 = ? and five_key_table.p2 = ? and five_key_table.p3 = ? and five_key_table.p4 = ? and five_key_table.p5 = ?",
                compiled.sql()
        );
        assertEquals(List.of("updated", 1L, 2L, 3L, 4L, 5L), compiled.params());
    }

    @Test
    void compilesDeleteByIdForFiveColumnPrimaryKey() {
        CompiledSql compiled = DSL.delete(FIVE_KEY)
                .byId(1L, 2L, 3L, 4L, 5L)
                .compile();

        assertEquals(
                "delete from five_key_table where five_key_table.p1 = ? and five_key_table.p2 = ? and five_key_table.p3 = ? and five_key_table.p4 = ? and five_key_table.p5 = ?",
                compiled.sql()
        );
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), compiled.params());
    }

    @Test
    void compilesJoinByForFiveColumns() {
        FiveKeyTable left = FIVE_KEY.as("l");
        FiveKeyTable right = FIVE_KEY.as("r");

        Query<FiveKeyTable> query = DSL.from(left)
                .join(right)
                .by(Cols.of(left.p1, left.p2, left.p3, left.p4, left.p5))
                .select(left.payload)
                .build();

        CompiledSql compiled = new SqlCompiler().compile(query);

        assertEquals(
                "SELECT l.payload FROM five_key_table l JOIN five_key_table r ON l.p1 = r.p1 AND l.p2 = r.p2 AND l.p3 = r.p3 AND l.p4 = r.p4 AND l.p5 = r.p5",
                compiled.sql()
        );
        assertEquals(List.of(), compiled.params());
    }

    private static final class TripleKeyTable extends TableDef3<TripleKeyTable, Long, Long, Long> {
        private final Cols3<TripleKeyTable, Long, Long, Long> primaryKey;

        private final ColumnDef<TripleKeyTable, Long> p1 = col("p1", Long.class);
        private final ColumnDef<TripleKeyTable, Long> p2 = col("p2", Long.class);
        private final ColumnDef<TripleKeyTable, Long> p3 = col("p3", Long.class);

        private TripleKeyTable(String name, String alias) {
            super(name, alias);
            primaryKey = Cols.of(p1, p2, p3);
        }

        @Override
        public Cols3<TripleKeyTable, Long, Long, Long> primaryKeyCols() {
            return primaryKey;
        }

        @Override
        public TripleKeyTable as(String alias) {
            return new TripleKeyTable(name(), alias);
        }
    }

    private static final class QuadKeyTable extends TableDef4<QuadKeyTable, Long, Long, Long, Long> {
        private final Cols4<QuadKeyTable, Long, Long, Long, Long> primaryKey;

        private final ColumnDef<QuadKeyTable, Long> p1 = col("p1", Long.class);
        private final ColumnDef<QuadKeyTable, Long> p2 = col("p2", Long.class);
        private final ColumnDef<QuadKeyTable, Long> p3 = col("p3", Long.class);
        private final ColumnDef<QuadKeyTable, Long> p4 = col("p4", Long.class);

        private QuadKeyTable(String name, String alias) {
            super(name, alias);
            primaryKey = Cols.of(p1, p2, p3, p4);
        }

        @Override
        public Cols4<QuadKeyTable, Long, Long, Long, Long> primaryKeyCols() {
            return primaryKey;
        }

        @Override
        public QuadKeyTable as(String alias) {
            return new QuadKeyTable(name(), alias);
        }
    }

    private static final class FiveKeyTable extends TableDef5<FiveKeyTable, Long, Long, Long, Long, Long> {
        private final Cols5<FiveKeyTable, Long, Long, Long, Long, Long> primaryKey;

        private final ColumnDef<FiveKeyTable, Long> p1 = col("p1", Long.class);
        private final ColumnDef<FiveKeyTable, Long> p2 = col("p2", Long.class);
        private final ColumnDef<FiveKeyTable, Long> p3 = col("p3", Long.class);
        private final ColumnDef<FiveKeyTable, Long> p4 = col("p4", Long.class);
        private final ColumnDef<FiveKeyTable, Long> p5 = col("p5", Long.class);
        private final ColumnDef<FiveKeyTable, String> payload = col("payload", String.class);

        private FiveKeyTable(String name, String alias) {
            super(name, alias);
            primaryKey = Cols.of(p1, p2, p3, p4, p5);
        }

        @Override
        public Cols5<FiveKeyTable, Long, Long, Long, Long, Long> primaryKeyCols() {
            return primaryKey;
        }

        @Override
        public FiveKeyTable as(String alias) {
            return new FiveKeyTable(name(), alias);
        }
    }
}
