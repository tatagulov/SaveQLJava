package io.tatagulov.saveql.api.mapping;

import io.tatagulov.saveql.api.stage.MapStage;
import io.tatagulov.saveql.api.stage.SelectToStage;
import io.tatagulov.saveql.expr.Expr;
import io.tatagulov.saveql.query.Query;
import io.tatagulov.saveql.sql.CompiledMappedSql;
import io.tatagulov.saveql.sql.CompiledSql;
import io.tatagulov.saveql.sql.SqlCompiler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class MappedQueryBuilder<RootRow, T> implements SelectToStage<RootRow, T>, MapStage<RootRow, T> {
    private final Query<RootRow> query;
    private final Supplier<T> supplier;
    private final List<FieldMapping<T, ?>> mappings = new ArrayList<>();

    public MappedQueryBuilder(Query<RootRow> query, Supplier<T> supplier) {
        this.query = Objects.requireNonNull(query, "query");
        this.supplier = Objects.requireNonNull(supplier, "supplier");
    }

    @Override
    public <M> MapStage<RootRow, T> map(Expr<M> expr, BiConsumer<T, M> setter) {
        Objects.requireNonNull(expr, "expr");
        Objects.requireNonNull(setter, "setter");

        mappings.add(new FieldMapping<>(expr, setter));
        return this;
    }

    @Override
    public CompiledMappedSql<T> compile() {
        var selectExpr = mappings.stream().map(FieldMapping::expr).toList().toArray(Expr[]::new);
        query.select(selectExpr);
        CompiledSql compiled = new SqlCompiler().compile(query);
        return new CompiledMappedSql<>(compiled.sql(), compiled.params(), this::mapRow);
    }

    @Override
    public List<T> fetch(Connection connection) throws SQLException {
        return compile().fetch(connection);
    }

    @Override
    public Optional<T> fetchOne(Connection connection) throws SQLException {
        return compile().fetchOne(connection);
    }

    private T mapRow(ResultSet rs) throws SQLException {
        T dto = supplier.get();
        if (dto == null) {
            throw new SQLException("DTO supplier returned null");
        }
        int columnIndex = 1;
        for (FieldMapping<T, ?> mapping : mappings) {
            mapping.apply(dto, rs, columnIndex);
            columnIndex++;
        }
        return dto;
    }



    private record FieldMapping<T, M>(Expr<M> expr, BiConsumer<T, M> setter) {
        private FieldMapping {
            Objects.requireNonNull(expr, "expr");
            Objects.requireNonNull(setter, "setter");
        }

        void apply(T dto, ResultSet resultSet, Integer index) throws SQLException {
            M value = resultSet.getObject(index, expr.getType());
            setter.accept(dto, value);
        }
    }
}
