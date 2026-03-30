package io.tatagulov.saveql.generator.mapping;

import java.sql.Types;
import java.util.Locale;

public final class DefaultJdbcTypeMapper implements JdbcTypeMapper {
    @Override
    public String map(int jdbcType, String databaseTypeName, int decimalDigits, int nullable) {
        String normalizedTypeName = databaseTypeName == null ? "" : databaseTypeName.toLowerCase(Locale.ROOT);

        if ("timetz".equals(normalizedTypeName) || "time with time zone".equals(normalizedTypeName)) {
            return java.time.OffsetTime.class.getName();
        }
        if ("timestamptz".equals(normalizedTypeName) || "timestamp with time zone".equals(normalizedTypeName)) {
            return java.time.OffsetDateTime.class.getName();
        }

        return switch (jdbcType) {
            case Types.BIGINT -> Long.class.getName();
            case Types.INTEGER, Types.SMALLINT, Types.TINYINT -> Integer.class.getName();
            case Types.BIT, Types.BOOLEAN -> Boolean.class.getName();
            case Types.NUMERIC, Types.DECIMAL -> decimalDigits == 0 ? Long.class.getName() : java.math.BigDecimal.class.getName();
            case Types.FLOAT, Types.REAL -> Float.class.getName();
            case Types.DOUBLE -> Double.class.getName();
            case Types.CHAR, Types.NCHAR, Types.VARCHAR, Types.NVARCHAR, Types.LONGVARCHAR, Types.LONGNVARCHAR -> String.class.getName();
            case Types.DATE -> java.time.LocalDate.class.getName();
            case Types.TIME, Types.TIME_WITH_TIMEZONE -> java.time.OffsetTime.class.getName();
            case Types.TIMESTAMP -> java.time.LocalDateTime.class.getName();
            case Types.TIMESTAMP_WITH_TIMEZONE -> java.time.OffsetDateTime.class.getName();
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> "byte[]";
            case Types.OTHER -> "uuid".equalsIgnoreCase(databaseTypeName) ? java.util.UUID.class.getName() : Object.class.getName();
            default -> Object.class.getName();
        };
    }
}
