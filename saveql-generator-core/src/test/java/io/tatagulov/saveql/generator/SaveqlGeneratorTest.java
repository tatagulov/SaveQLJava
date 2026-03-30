package io.tatagulov.saveql.generator;

import io.tatagulov.saveql.generator.codegen.JavaSourceFile;
import io.tatagulov.saveql.generator.codegen.SaveqlSchemaJavaGenerator;
import io.tatagulov.saveql.generator.config.GeneratorConfig;
import io.tatagulov.saveql.generator.model.ColumnModel;
import io.tatagulov.saveql.generator.model.DatabaseModel;
import io.tatagulov.saveql.generator.model.PrimaryKeyModel;
import io.tatagulov.saveql.generator.model.TableModel;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveqlGeneratorTest {

    @Test
    void generatesTableAndSchemaFilesFromDatabaseModel() {
        DatabaseModel model = new DatabaseModel(List.of(
                new TableModel(
                        null,
                        "public",
                        "users",
                        "UsersTable",
                        "USERS",
                        List.of(
                                new ColumnModel("id", "id", "java.util.UUID", 0, "uuid", false, false, false, 1),
                                new ColumnModel("name", "name", "java.lang.String", 0, "varchar", false, false, false, 2)
                        ),
                        new PrimaryKeyModel(List.of("id"))
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

        SaveqlSchemaJavaGenerator generator = new SaveqlSchemaJavaGenerator();
        List<JavaSourceFile> files = generator.generate(model, config);

        assertEquals(2, files.size());

        String tableFile = files.stream()
                .filter(file -> file.relativePath().endsWith("UsersTable.java"))
                .findFirst()
                .orElseThrow()
                .content();
        String schemaFile = files.stream()
                .filter(file -> file.relativePath().endsWith("AppSchema.java"))
                .findFirst()
                .orElseThrow()
                .content();

        assertTrue(tableFile.contains("public final class UsersTable extends TableDef1<UsersTable, UUID>"));
        assertTrue(tableFile.contains("id = col(\"id\", UUID.class, false, false, false);"));
        assertTrue(tableFile.contains("private final Cols1<UsersTable, UUID> primaryKeyCols;"));
        assertTrue(tableFile.contains("primaryKeyCols = Cols.of(id);"));
        assertTrue(schemaFile.contains("public static final UsersTable USERS = new UsersTable();"));
    }
}
