package io.tatagulov.saveql.generator.cli;

import io.tatagulov.saveql.generator.SaveqlGenerator;
import io.tatagulov.saveql.generator.config.GeneratorConfig;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class SaveqlGeneratorCli {
    private SaveqlGeneratorCli() {
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> arguments = parseArgs(args);
        GeneratorConfig config = new GeneratorConfig(
                required(arguments, "jdbc-url"),
                arguments.get("username"),
                arguments.get("password"),
                required(arguments, "package-name"),
                arguments.get("schema-name"),
                arguments.get("catalog-name"),
                arguments.getOrDefault("table-pattern", "%"),
                arguments.getOrDefault("schema-class-name", "GeneratedSchema"),
                Path.of(required(arguments, "output-dir"))
        );

        var files = new SaveqlGenerator().generate(config);
        System.out.println("Generated " + files.size() + " file(s) into " + config.outputDirectory());
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> values = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (!arg.startsWith("--")) {
                throw new IllegalArgumentException("Unexpected argument: " + arg);
            }
            if (i + 1 >= args.length) {
                throw new IllegalArgumentException("Missing value for argument: " + arg);
            }
            values.put(arg.substring(2), args[++i]);
        }
        return values;
    }

    private static String required(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required argument --" + key);
        }
        return value;
    }
}
