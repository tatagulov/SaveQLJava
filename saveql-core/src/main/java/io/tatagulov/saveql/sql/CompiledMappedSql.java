package io.tatagulov.saveql.sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class CompiledMappedSql<T> extends CompiledSql {
    private final SqlRowMapper<T> rowMapper;

    public CompiledMappedSql(String sql, List<Object> params, SqlRowMapper<T> rowMapper) {
        super(sql, params);
        this.rowMapper = Objects.requireNonNull(rowMapper, "rowMapper");
    }

    public SqlRowMapper<T> rowMapper() {
        return rowMapper;
    }

    public List<T> fetch(Connection connection) throws SQLException {
        Objects.requireNonNull(connection, "connection");
        try (PreparedStatement statement = connection.prepareStatement(sql())) {
            bindParams(statement);
            try (ResultSet rs = statement.executeQuery()) {
                return mapAll(rs);
            }
        }
    }

    public List<T> fetch(DataSource dataSource) throws SQLException {
        Objects.requireNonNull(dataSource, "dataSource");
        try (Connection connection = dataSource.getConnection()) {
            return fetch(connection);
        }
    }

    public Optional<T> fetchOne(Connection connection) throws SQLException {
        List<T> list = fetch(connection);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        if (list.size() > 1) {
            throw new SQLException("Expected at most one row but got " + list.size());
        }
        return Optional.ofNullable(list.get(0));
    }

    public Optional<T> fetchOne(DataSource dataSource) throws SQLException {
        Objects.requireNonNull(dataSource, "dataSource");
        try (Connection connection = dataSource.getConnection()) {
            return fetchOne(connection);
        }
    }

    private void bindParams(PreparedStatement statement) throws SQLException {
        for (int i = 0; i < params().size(); i++) {
            statement.setObject(i + 1, params().get(i));
        }
    }

    private List<T> mapAll(ResultSet rs) throws SQLException {
        List<T> out = new ArrayList<>();
        while (rs.next()) {
            out.add(rowMapper.map(rs));
        }
        return out;
    }
}
