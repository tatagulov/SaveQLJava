package io.tatagulov.saveql.generator;

import io.tatagulov.saveql.generator.codegen.JavaSourceFile;
import io.tatagulov.saveql.generator.codegen.JavaSourceWriter;
import io.tatagulov.saveql.generator.codegen.SaveqlSchemaJavaGenerator;
import io.tatagulov.saveql.generator.config.GeneratorConfig;
import io.tatagulov.saveql.generator.jdbc.JdbcMetadataReader;
import io.tatagulov.saveql.generator.mapping.DefaultJdbcTypeMapper;
import io.tatagulov.saveql.generator.model.DatabaseModel;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public final class SaveqlGenerator {
    private final JdbcMetadataReader metadataReader;
    private final SaveqlSchemaJavaGenerator javaGenerator;
    private final JavaSourceWriter sourceWriter;

    public SaveqlGenerator() {
        this(new JdbcMetadataReader(new DefaultJdbcTypeMapper()), new SaveqlSchemaJavaGenerator(), new JavaSourceWriter());
    }

    public SaveqlGenerator(
            JdbcMetadataReader metadataReader,
            SaveqlSchemaJavaGenerator javaGenerator,
            JavaSourceWriter sourceWriter
    ) {
        this.metadataReader = metadataReader;
        this.javaGenerator = javaGenerator;
        this.sourceWriter = sourceWriter;
    }

    public List<JavaSourceFile> preview(GeneratorConfig config) throws SQLException {
        DatabaseModel model = metadataReader.read(config);
        return javaGenerator.generate(model, config);
    }

    public List<JavaSourceFile> generate(GeneratorConfig config) throws SQLException, IOException {
        List<JavaSourceFile> files = preview(config);
        sourceWriter.write(config.outputDirectory(), files);
        return files;
    }
}
