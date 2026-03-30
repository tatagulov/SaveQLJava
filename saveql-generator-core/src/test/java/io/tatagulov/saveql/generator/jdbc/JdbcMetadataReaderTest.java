package io.tatagulov.saveql.generator.jdbc;

import io.tatagulov.saveql.generator.config.GeneratorConfig;
import io.tatagulov.saveql.generator.mapping.JdbcTypeMapper;
import io.tatagulov.saveql.generator.model.DatabaseModel;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JdbcMetadataReaderTest {
    @Test
    void skipsTablesWithoutPrimaryKey() throws Exception {
        JdbcTypeMapper typeMapper = (jdbcType, databaseTypeName, decimalDigits, nullable) -> "java.lang.String";
        JdbcMetadataReader reader = new JdbcMetadataReader(typeMapper);

        Connection connection = connection(metadata(
                List.of(
                        Map.of(
                                "TABLE_CAT", "app",
                                "TABLE_SCHEM", "public",
                                "TABLE_NAME", "users"
                        ),
                        Map.of(
                                "TABLE_CAT", "app",
                                "TABLE_SCHEM", "public",
                                "TABLE_NAME", "audit_log"
                        )
                ),
                Map.of(
                        "users", List.of(
                                row(
                                        "COLUMN_NAME", "id",
                                        "DATA_TYPE", Types.VARCHAR,
                                        "TYPE_NAME", "varchar",
                                        "DECIMAL_DIGITS", 0,
                                        "NULLABLE", DatabaseMetaData.columnNoNulls,
                                        "COLUMN_DEF", null,
                                        "IS_AUTOINCREMENT", "NO",
                                        "IS_GENERATEDCOLUMN", "NO",
                                        "ORDINAL_POSITION", 1
                                ),
                                row(
                                        "COLUMN_NAME", "name",
                                        "DATA_TYPE", Types.VARCHAR,
                                        "TYPE_NAME", "varchar",
                                        "DECIMAL_DIGITS", 0,
                                        "NULLABLE", DatabaseMetaData.columnNoNulls,
                                        "COLUMN_DEF", null,
                                        "IS_AUTOINCREMENT", "NO",
                                        "IS_GENERATEDCOLUMN", "NO",
                                        "ORDINAL_POSITION", 2
                                )
                        ),
                        "audit_log", List.of(
                                row(
                                        "COLUMN_NAME", "id",
                                        "DATA_TYPE", Types.VARCHAR,
                                        "TYPE_NAME", "varchar",
                                        "DECIMAL_DIGITS", 0,
                                        "NULLABLE", DatabaseMetaData.columnNoNulls,
                                        "COLUMN_DEF", null,
                                        "IS_AUTOINCREMENT", "NO",
                                        "IS_GENERATEDCOLUMN", "NO",
                                        "ORDINAL_POSITION", 1
                                )
                        )
                ),
                Map.of(
                        "users", List.of(Map.of(
                                "KEY_SEQ", (short) 1,
                                "COLUMN_NAME", "id"
                        )),
                        "audit_log", List.of()
                )
        ));

        GeneratorConfig config = new GeneratorConfig(
                "jdbc:test",
                null,
                null,
                "com.example.generated",
                "public",
                null,
                "%",
                "AppSchema",
                Path.of("target/generated-test-sources")
        );

        DatabaseModel model = reader.read(connection, config);

        assertEquals(1, model.tables().size());
        assertEquals("users", model.tables().get(0).tableName());
        assertEquals(List.of("id"), model.tables().get(0).primaryKey().columnNames());
    }

    private static DatabaseMetaData metadata(
            List<Map<String, Object>> tables,
            Map<String, List<Map<String, Object>>> columnsByTable,
            Map<String, List<Map<String, Object>>> primaryKeysByTable
    ) {
        return (DatabaseMetaData) Proxy.newProxyInstance(
                DatabaseMetaData.class.getClassLoader(),
                new Class[]{DatabaseMetaData.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getTables" -> rows(tables);
                    case "getColumns" -> rows(columnsByTable.getOrDefault((String) args[2], Collections.emptyList()));
                    case "getPrimaryKeys" -> rows(primaryKeysByTable.getOrDefault((String) args[2], Collections.emptyList()));
                    case "toString" -> "stub-metadata";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException("Unsupported DatabaseMetaData method: " + method.getName());
                }
        );
    }

    private static Connection connection(DatabaseMetaData metadata) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getMetaData" -> metadata;
                    case "close" -> null;
                    case "isClosed" -> false;
                    case "toString" -> "stub-connection";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException("Unsupported Connection method: " + method.getName());
                }
        );
    }

    private static ResultSet rows(List<Map<String, Object>> rows) {
        return rows(rows.toArray(Map[]::new));
    }

    @SafeVarargs
    private static ResultSet rows(Map<String, Object>... rows) {
        List<Map<String, Object>> data = List.of(rows);
        int[] index = {-1};
        return (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class[]{ResultSet.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "next" -> ++index[0] < data.size();
                    case "getString" -> value(data, index[0], (String) args[0]);
                    case "getInt" -> ((Number) value(data, index[0], (String) args[0])).intValue();
                    case "getShort" -> ((Number) value(data, index[0], (String) args[0])).shortValue();
                    case "close" -> null;
                    case "toString" -> "stub-result-set";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException("Unsupported ResultSet method: " + method.getName());
                }
        );
    }

    private static Object value(List<Map<String, Object>> rows, int index, String columnName) {
        if (index < 0 || index >= rows.size()) {
            throw new IllegalStateException("ResultSet cursor is not positioned on a row");
        }
        return rows.get(index).get(columnName);
    }

    private static Map<String, Object> row(Object... keyValues) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            row.put((String) keyValues[i], keyValues[i + 1]);
        }
        return row;
    }
}
