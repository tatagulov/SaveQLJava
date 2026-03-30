package io.tatagulov.saveql.generator.mapping;

public interface JdbcTypeMapper {
    String map(int jdbcType, String databaseTypeName, int decimalDigits, int nullable);
}
