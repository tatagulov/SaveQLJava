package io.tatagulov.saveql.generator.codegen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class JavaSourceWriter {
    public void write(Path rootDirectory, List<JavaSourceFile> files) throws IOException {
        for (JavaSourceFile file : files) {
            Path output = rootDirectory.resolve(file.relativePath());
            Files.createDirectories(output.getParent());
            Files.writeString(output, file.content(), StandardCharsets.UTF_8);
        }
    }
}
