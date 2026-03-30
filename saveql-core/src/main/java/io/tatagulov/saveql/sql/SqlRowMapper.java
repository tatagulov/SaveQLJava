package io.tatagulov.saveql.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlRowMapper<T> {
    T map(ResultSet rs) throws SQLException;
}
