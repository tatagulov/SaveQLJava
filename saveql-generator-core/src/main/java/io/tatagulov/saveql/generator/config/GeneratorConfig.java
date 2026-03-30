package io.tatagulov.saveql.generator.config;

import java.nio.file.Path;
import java.util.Objects;

public record GeneratorConfig(
        String jdbcUrl,
        String username,
        String password,
        String packageName,
        String schemaName,
        String catalogName,
        String tableNamePattern,
        String schemaClassName,
        Path outputDirectory
) {
    public GeneratorConfig {
        Objects.requireNonNull(jdbcUrl, "jdbcUrl");
        Objects.requireNonNull(packageName, "packageName");
        Objects.requireNonNull(schemaClassName, "schemaClassName");
        Objects.requireNonNull(outputDirectory, "outputDirectory");
    }
}
