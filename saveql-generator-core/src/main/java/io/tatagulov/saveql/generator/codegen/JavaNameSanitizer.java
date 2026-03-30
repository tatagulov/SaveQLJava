package io.tatagulov.saveql.generator.codegen;

import java.util.Locale;
import java.util.Set;

public final class JavaNameSanitizer {
    private static final Set<String> JAVA_KEYWORDS = Set.of(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
            "volatile", "while", "record", "sealed", "permits", "non-sealed", "var", "yield"
    );

    private JavaNameSanitizer() {
    }

    public static String toClassName(String value) {
        return sanitize(toPascalCase(value));
    }

    public static String toFieldName(String value) {
        String camel = toCamelCase(value);
        return sanitize(camel.isBlank() ? "field" : camel);
    }

    public static String toConstantName(String value) {
        String normalized = value.replaceAll("[^A-Za-z0-9]+", "_").toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return "TABLE";
        }
        if (Character.isDigit(normalized.charAt(0))) {
            normalized = "T_" + normalized;
        }
        return normalized;
    }

    private static String toPascalCase(String value) {
        String[] parts = value.split("[^A-Za-z0-9]+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        if (builder.isEmpty()) {
            return "Generated";
        }
        if (!builder.toString().endsWith("Table")) {
            builder.append("Table");
        }
        return builder.toString();
    }

    private static String toCamelCase(String value) {
        String pascal = toPascalCase(value).replaceFirst("Table$", "");
        if (pascal.isBlank()) {
            return "field";
        }
        return Character.toLowerCase(pascal.charAt(0)) + pascal.substring(1);
    }

    private static String sanitize(String candidate) {
        String value = candidate;
        if (Character.isDigit(value.charAt(0))) {
            value = "_" + value;
        }
        if (JAVA_KEYWORDS.contains(value)) {
            value = value + "Value";
        }
        return value;
    }
}
