package io.tatagulov.saveql.generator.codegen;

import io.tatagulov.saveql.generator.config.GeneratorConfig;
import io.tatagulov.saveql.generator.model.ColumnModel;
import io.tatagulov.saveql.generator.model.DatabaseModel;
import io.tatagulov.saveql.generator.model.TableModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class SaveqlSchemaJavaGenerator {
    public List<JavaSourceFile> generate(DatabaseModel model, GeneratorConfig config) {
        List<TableModel> tables = model.tables().stream()
                .sorted(Comparator.comparing(TableModel::javaClassName))
                .toList();

        List<JavaSourceFile> files = new ArrayList<>();
        for (TableModel table : tables) {
            files.add(new JavaSourceFile(
                    packageToPath(config.packageName()) + "/" + table.javaClassName() + ".java",
                    generateTableClass(table, config.packageName())
            ));
        }
        files.add(new JavaSourceFile(
                packageToPath(config.packageName()) + "/" + config.schemaClassName() + ".java",
                generateSchemaClass(tables, config.packageName(), config.schemaClassName())
        ));
        return files;
    }

    private String generateTableClass(TableModel table, String packageName) {
        Set<String> imports = new LinkedHashSet<>();
        imports.add("io.tatagulov.saveql.schema.Cols");
        imports.add("io.tatagulov.saveql.schema." + colsClass(table));
        imports.add("io.tatagulov.saveql.schema.ColumnDef");
        imports.add("io.tatagulov.saveql.schema." + tableDefClass(table));

        for (ColumnModel column : table.columns()) {
            if (column.javaTypeName().contains(".")) {
                imports.add(column.javaTypeName());
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(packageName).append(";\n\n");
        imports.forEach(value -> builder.append("import ").append(value).append(";\n"));
        builder.append('\n');
        builder.append("public final class ").append(table.javaClassName())
                .append(" extends ").append(tableDefClass(table)).append('<')
                .append(table.javaClassName()).append(primaryKeyTypeParameters(table)).append("> {\n");

        for (ColumnModel column : table.columns()) {
            builder.append("    public final ColumnDef<").append(table.javaClassName()).append(", ")
                    .append(simpleTypeName(column.javaTypeName())).append("> ")
                    .append(column.fieldName()).append(";\n");
        }
        builder.append('\n');
        builder.append("    private final ").append(colsClass(table)).append('<')
                .append(table.javaClassName()).append(primaryKeyTypeParameters(table)).append("> primaryKeyCols;\n\n");
        builder.append("    public ").append(table.javaClassName()).append("() {\n")
                .append("        this(null);\n")
                .append("    }\n\n");
        builder.append("    private ").append(table.javaClassName()).append("(String alias) {\n")
                .append("        super(\"").append(table.tableName()).append("\", alias);\n");
        for (ColumnModel column : table.columns()) {
            builder.append("        ").append(column.fieldName()).append(" = col(\"").append(column.columnName()).append("\", ")
                    .append(simpleTypeName(column.javaTypeName())).append(".class, ")
                    .append(column.nullable()).append(", ")
                    .append(column.hasDefaultValue()).append(", ")
                    .append(column.generated()).append(");\n");
        }
        builder.append("        primaryKeyCols = Cols.of(")
                .append(table.primaryKey().columnNames().stream().map(this::toFieldReference).collect(Collectors.joining(", ")))
                .append(");\n");
        builder.append("    }\n\n");
        builder.append("    @Override\n")
                .append("    public ").append(colsClass(table)).append('<')
                .append(table.javaClassName()).append(primaryKeyTypeParameters(table)).append("> primaryKeyCols() {\n")
                .append("        return primaryKeyCols;\n")
                .append("    }\n\n");
        builder.append("    @Override\n")
                .append("    public ").append(table.javaClassName()).append(" as(String alias) {\n")
                .append("        return new ").append(table.javaClassName()).append("(alias);\n")
                .append("    }\n")
                .append("}\n");
        return builder.toString();
    }

    private String generateSchemaClass(List<TableModel> tables, String packageName, String schemaClassName) {
        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(packageName).append(";\n\n");
        builder.append("public final class ").append(schemaClassName).append(" {\n");
        builder.append("    private ").append(schemaClassName).append("() {\n")
                .append("    }\n\n");
        for (TableModel table : tables) {
            builder.append("    public static final ").append(table.javaClassName()).append(' ')
                    .append(table.constantName()).append(" = new ").append(table.javaClassName()).append("();\n");
        }
        builder.append("}\n");
        return builder.toString();
    }

    private String packageToPath(String packageName) {
        return packageName.replace('.', '/');
    }

    private String simpleTypeName(String javaTypeName) {
        if (javaTypeName.endsWith("[]")) {
            int packageEnd = javaTypeName.lastIndexOf('.');
            return packageEnd >= 0 ? javaTypeName.substring(packageEnd + 1) : javaTypeName;
        }
        int lastDot = javaTypeName.lastIndexOf('.');
        return lastDot >= 0 ? javaTypeName.substring(lastDot + 1) : javaTypeName;
    }

    private String tableDefClass(TableModel table) {
        return switch (table.primaryKey().size()) {
            case 1 -> "TableDef1";
            case 2 -> "TableDef2";
            case 3 -> "TableDef3";
            case 4 -> "TableDef4";
            case 5 -> "TableDef5";
            default -> throw new IllegalArgumentException("Only primary keys with 1 to 5 columns are supported for table " + table.tableName());
        };
    }

    private String colsClass(TableModel table) {
        return switch (table.primaryKey().size()) {
            case 1 -> "Cols1";
            case 2 -> "Cols2";
            case 3 -> "Cols3";
            case 4 -> "Cols4";
            case 5 -> "Cols5";
            default -> throw new IllegalArgumentException("Only primary keys with 1 to 5 columns are supported for table " + table.tableName());
        };
    }

    private String primaryKeyTypeParameters(TableModel table) {
        return primaryKeyColumns(table).stream()
                .map(ColumnModel::javaTypeName)
                .map(this::simpleTypeName)
                .collect(Collectors.joining(", ", ", ", ""));
    }

    private List<ColumnModel> primaryKeyColumns(TableModel table) {
        return table.primaryKey().columnNames().stream()
                .map(columnName -> table.columns().stream()
                        .filter(column -> column.columnName().equals(columnName))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Primary key column " + columnName + " not found in table " + table.tableName())))
                .toList();
    }

    private String toFieldReference(String columnName) {
        return JavaNameSanitizer.toFieldName(columnName);
    }
}
