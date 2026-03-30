package io.tatagulov.saveql.maven;

import io.tatagulov.saveql.generator.SaveqlGenerator;
import io.tatagulov.saveql.generator.config.GeneratorConfig;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class GenerateSchemaMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "saveql.jdbcUrl", required = true)
    private String jdbcUrl;

    @Parameter(property = "saveql.username")
    private String username;

    @Parameter(property = "saveql.password")
    private String password;

    @Parameter(property = "saveql.packageName", required = true)
    private String packageName;

    @Parameter(property = "saveql.schemaName")
    private String schemaName;

    @Parameter(property = "saveql.catalogName")
    private String catalogName;

    @Parameter(property = "saveql.tableNamePattern", defaultValue = "%")
    private String tableNamePattern;

    @Parameter(property = "saveql.schemaClassName", defaultValue = "GeneratedSchema")
    private String schemaClassName;

    @Parameter(property = "saveql.outputDirectory", defaultValue = "${project.build.directory}/generated-sources/saveql")
    private File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        GeneratorConfig config = new GeneratorConfig(
                jdbcUrl,
                username,
                password,
                packageName,
                schemaName,
                catalogName,
                tableNamePattern,
                schemaClassName,
                outputDirectory.toPath()
        );

        try {
            var files = new SaveqlGenerator().generate(config);
            project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
            getLog().info("Generated " + files.size() + " SaveQL source file(s) into " + outputDirectory);
        } catch (SQLException | IOException e) {
            throw new MojoExecutionException("Failed to generate SaveQL schema", e);
        }
    }
}
