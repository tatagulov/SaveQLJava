package io.tatagulov.saveql.generator.jdbc;

import io.tatagulov.saveql.generator.codegen.JavaNameSanitizer;
import io.tatagulov.saveql.generator.config.GeneratorConfig;
import io.tatagulov.saveql.generator.mapping.JdbcTypeMapper;
import io.tatagulov.saveql.generator.model.ColumnModel;
import io.tatagulov.saveql.generator.model.DatabaseModel;
import io.tatagulov.saveql.generator.model.PrimaryKeyModel;
import io.tatagulov.saveql.generator.model.TableModel;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JdbcMetadataReader {
    private final JdbcTypeMapper typeMapper;

    public JdbcMetadataReader(JdbcTypeMapper typeMapper) {
        this.typeMapper = typeMapper;
    }

    public DatabaseModel read(GeneratorConfig config) throws SQLException {
        try (Connection connection = DriverManager.getConnection(config.jdbcUrl(), config.username(), config.password())) {
            return read(connection, config);
        }
    }

    public DatabaseModel read(Connection connection, GeneratorConfig config) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        List<TableModel> tables = new ArrayList<>();
        try (ResultSet rs = metadata.getTables(config.catalogName(), config.schemaName(), config.tableNamePattern(), new String[]{"TABLE"})) {
            while (rs.next()) {
                String catalog = rs.getString("TABLE_CAT");
                String schema = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");
                List<ColumnModel> columns = readColumns(metadata, catalog, schema, tableName);
                PrimaryKeyModel primaryKey = readPrimaryKey(metadata, catalog, schema, tableName);
                if (primaryKey == null) {
                    continue;
                }
                tables.add(new TableModel(
                        catalog,
                        schema,
                        tableName,
                        JavaNameSanitizer.toClassName(tableName),
                        JavaNameSanitizer.toConstantName(tableName),
                        columns,
                        primaryKey
                ));
            }
        }
        return new DatabaseModel(tables);
    }

    private List<ColumnModel> readColumns(DatabaseMetaData metadata, String catalog, String schema, String tableName) throws SQLException {
        List<ColumnModel> columns = new ArrayList<>();
        try (ResultSet rs = metadata.getColumns(catalog, schema, tableName, null)) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                int dataType = rs.getInt("DATA_TYPE");
                String typeName = rs.getString("TYPE_NAME");
                int decimalDigits = rs.getInt("DECIMAL_DIGITS");
                int nullable = rs.getInt("NULLABLE");
                String columnDefault = rs.getString("COLUMN_DEF");
                String isAutoIncrement = rs.getString("IS_AUTOINCREMENT");
                String isGeneratedColumn = rs.getString("IS_GENERATEDCOLUMN");
                columns.add(new ColumnModel(
                        columnName,
                        JavaNameSanitizer.toFieldName(columnName),
                        typeMapper.map(dataType, typeName, decimalDigits, nullable),
                        dataType,
                        typeName,
                        nullable == DatabaseMetaData.columnNullable,
                        columnDefault != null,
                        "YES".equalsIgnoreCase(isAutoIncrement) || "YES".equalsIgnoreCase(isGeneratedColumn),
                        rs.getInt("ORDINAL_POSITION")
                ));
            }
        }
        columns.sort(Comparator.comparingInt(ColumnModel::ordinalPosition));
        return columns;
    }

    private PrimaryKeyModel readPrimaryKey(DatabaseMetaData metadata, String catalog, String schema, String tableName) throws SQLException {
        Map<Short, String> columns = new HashMap<>();
        try (ResultSet rs = metadata.getPrimaryKeys(catalog, schema, tableName)) {
            while (rs.next()) {
                columns.put(rs.getShort("KEY_SEQ"), rs.getString("COLUMN_NAME"));
            }
        }
        List<String> primaryKeyColumns = columns.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .toList();
        if (primaryKeyColumns.isEmpty()) {
            return null;
        }
        if (primaryKeyColumns.size() > 5) {
            throw new IllegalArgumentException("Table " + tableName + " has " + primaryKeyColumns.size() + " primary key columns. Only 1 to 5 are supported now.");
        }
        return new PrimaryKeyModel(primaryKeyColumns);
    }
}
