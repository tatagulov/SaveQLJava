package io.tatagulov.saveql.api.stage;

import io.tatagulov.saveql.sql.CompiledMappedSql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface MapStage<RootRow, T> extends SelectToStage<RootRow, T> {

    CompiledMappedSql<T> compile();
    List<T> fetch(Connection connection) throws SQLException;
    Optional<T> fetchOne(Connection connection) throws SQLException;
}
