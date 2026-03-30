# SaveQL Java

SaveQL is a Java library and code generator for building SQL queries through a typed DSL instead of handwritten SQL strings.

The project is designed to make database access more predictable by:

- generating Java schema classes from a real database
- building queries from typed table and column objects
- catching structural mistakes earlier in code instead of at runtime
- reducing manual SQL string assembly for `SELECT`, `INSERT`, `UPDATE`, and `DELETE`

The repository contains the runtime DSL, the schema generator, a CLI, and a Maven plugin.

## Why this project exists

Writing SQL directly in application code is flexible, but it is also easy to break:

- table and column names are stringly typed
- refactors are hard to track safely
- joins and aggregates are easy to express incorrectly
- insert and update statements often duplicate schema knowledge already stored in the database

SaveQL addresses that by turning database metadata into Java classes and then using those classes to build queries with compile-time guidance.

For read queries, the project follows a root-oriented DSL model described in [safeql-spec.md](/home/dev/myProject/SaveQLJava/safeql-spec.md). The DSL is built around explicit joins, typed expressions, and predictable aggregation behavior.

## Project modules

- `saveql-core`: the runtime DSL, schema abstractions, SQL compiler, and query execution helpers
- `saveql-generator-core`: reads JDBC metadata and generates Java schema sources
- `saveql-generator-cli`: command-line wrapper around the generator
- `saveql-maven-plugin`: Maven plugin for generating schema classes during build
- `saveql-integration-tests`: optional PostgreSQL-based integration tests

## What problem it solves

SaveQL is useful when you want:

- typed access to tables and columns from Java
- generated schema code from an existing PostgreSQL or JDBC-accessible database
- query construction without embedding raw SQL everywhere
- safer refactoring of database-facing code
- a lightweight DSL instead of a full ORM

It is a good fit for projects that prefer explicit SQL-like behavior but still want stronger structure and code generation.

## Requirements

- Java 17
- Maven
- a database reachable over JDBC for schema generation

## Build

```bash
mvn test
```

To run integration tests as well:

```bash
mvn -q verify -Pintegration-tests
```

## How it works

The typical workflow is:

1. Point SaveQL at your database.
2. Generate Java schema classes for tables and columns.
3. Use those generated classes in application code with the DSL.
4. Compile queries into SQL or execute them through JDBC.

## Schema generation

### Maven plugin

Example plugin configuration:

```xml
<plugin>
    <groupId>io.tatagulov</groupId>
    <artifactId>saveql-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <jdbcUrl>jdbc:postgresql://localhost:5432/app</jdbcUrl>
        <username>postgres</username>
        <password>postgres</password>
        <packageName>com.example.generated</packageName>
        <schemaName>public</schemaName>
        <schemaClassName>AppSchema</schemaClassName>
    </configuration>
</plugin>
```

The plugin generates Java sources into `target/generated-sources/saveql` and adds that directory to the compile source roots.

### CLI

The CLI wraps the same generator:

```bash
java -jar saveql-generator-cli/target/saveql-generator-cli-1.0-SNAPSHOT.jar \
  --jdbc-url jdbc:postgresql://localhost:5432/app \
  --username postgres \
  --password postgres \
  --package-name com.example.generated \
  --schema-name public \
  --schema-class-name AppSchema \
  --output-dir ./generated
```

Supported arguments include:

- `--jdbc-url`
- `--username`
- `--password`
- `--package-name`
- `--schema-name`
- `--catalog-name`
- `--table-pattern`
- `--schema-class-name`
- `--output-dir`

## Usage examples

Assume the generator produced a schema class named `AppSchema`.

### 1. Simple select

```java
import io.tatagulov.saveql.api.DSL;
import io.tatagulov.saveql.query.Query;
import io.tatagulov.saveql.sql.CompiledSql;
import io.tatagulov.saveql.sql.SqlCompiler;

var users = AppSchema.APP_USER;

Query<?> query = DSL.from(users)
        .where(users.active.eq(true).and(users.score.gt(100L)))
        .select(users.id, users.displayName)
        .build();

CompiledSql sql = new SqlCompiler().compile(query);

System.out.println(sql.sql());
System.out.println(sql.params());
```

Produces SQL in the style of:

```sql
SELECT app_user.id, app_user.display_name
FROM app_user
WHERE app_user.active = ? and app_user.score > ?
```

### 2. Join and aggregate

```java
import static io.tatagulov.saveql.api.DSL.sum;

var orders = AppSchema.PURCHASE_ORDER.as("o");
var users = AppSchema.APP_USER.as("u");

var query = DSL.from(orders)
        .join(users).by(orders.userId)
        .where(orders.amount.gt(new BigDecimal("30.00")))
        .select(
                users.displayName,
                sum(orders.amount).as("total_amount")
        )
        .build();
```

This style keeps joins explicit and lets SaveQL compile aggregate expressions into SQL with the required grouping rules.

### 3. Map rows directly to a DTO

```java
List<UserSummaryDto> rows = DSL.from(AppSchema.APP_USER)
        .where(AppSchema.APP_USER.score.gt(70L))
        .orderBy(AppSchema.APP_USER.displayName.asc())
        .selectTo(UserSummaryDto::new)
        .map(AppSchema.APP_USER.id, UserSummaryDto::setId)
        .map(AppSchema.APP_USER.displayName, UserSummaryDto::setDisplayName)
        .map(AppSchema.APP_USER.score, UserSummaryDto::setScore)
        .map(AppSchema.APP_USER.balance, UserSummaryDto::setBalance)
        .fetch(connection);
```

### 4. Insert

```java
int inserted = DSL.insertInto(AppSchema.APP_USER)
        .set(AppSchema.APP_USER.id, userId)
        .set(AppSchema.APP_USER.displayName, "Cara")
        .set(AppSchema.APP_USER.email, "cara@example.com")
        .set(AppSchema.APP_USER.active, true)
        .set(AppSchema.APP_USER.score, 150L)
        .execute(connection);
```

SaveQL validates required and generated columns before execution.

### 5. Batch insert

```java
int[] inserted = DSL.insertInto(AppSchema.APP_USER)
        .batch(List.of(
                InsertRow.into(AppSchema.APP_USER)
                        .set(AppSchema.APP_USER.id, firstId)
                        .set(AppSchema.APP_USER.displayName, "Dina")
                        .set(AppSchema.APP_USER.email, "dina@example.com"),
                InsertRow.into(AppSchema.APP_USER)
                        .set(AppSchema.APP_USER.id, secondId)
                        .set(AppSchema.APP_USER.displayName, "Evan")
                        .set(AppSchema.APP_USER.email, "evan@example.com")
        ))
        .execute(connection);
```

### 6. Update

```java
int updated = DSL.update(AppSchema.APP_USER)
        .set(AppSchema.APP_USER.displayName, "Ann Updated")
        .set(AppSchema.APP_USER.balance, new BigDecimal("1600.00"))
        .byId(userId)
        .execute(connection);
```

Bulk update is also supported:

```java
int updated = DSL.update(AppSchema.PURCHASE_ORDER)
        .set(AppSchema.PURCHASE_ORDER.status, "ARCHIVED")
        .where(AppSchema.PURCHASE_ORDER.amount.lt(new BigDecimal("50.00")))
        .execute(connection);
```

### 7. Delete

```java
int deleted = DSL.delete(AppSchema.APP_USER)
        .byId(userId)
        .execute(connection);
```

Or a bulk delete:

```java
int deleted = DSL.delete(AppSchema.PURCHASE_ORDER)
        .where(AppSchema.PURCHASE_ORDER.amount.lt(new BigDecimal("10.00")))
        .execute(connection);
```

## Notes on query semantics

For read queries, SaveQL aims to make query structure explicit:

- every query starts from one root table
- joins are explicit and validated against key structure
- aggregate expressions are recognized by the compiler
- aggregate predicates are moved into `HAVING` when needed
- grouping is derived from the selected non-aggregate expressions

This is the main reason to use the project instead of stitching SQL strings manually.

## Current state

The repository is currently versioned as `1.0-SNAPSHOT`, so the API should be treated as under active development.

## Repository references

- DSL entry point: [saveql-core/src/main/java/io/tatagulov/saveql/api/DSL.java](/home/dev/myProject/SaveQLJava/saveql-core/src/main/java/io/tatagulov/saveql/api/DSL.java)
- CLI entry point: [saveql-generator-cli/src/main/java/io/tatagulov/saveql/generator/cli/SaveqlGeneratorCli.java](/home/dev/myProject/SaveQLJava/saveql-generator-cli/src/main/java/io/tatagulov/saveql/generator/cli/SaveqlGeneratorCli.java)
- Maven plugin: [saveql-maven-plugin/src/main/java/io/tatagulov/saveql/maven/GenerateSchemaMojo.java](/home/dev/myProject/SaveQLJava/saveql-maven-plugin/src/main/java/io/tatagulov/saveql/maven/GenerateSchemaMojo.java)
- SafeQL read-query model: [safeql-spec.md](/home/dev/myProject/SaveQLJava/safeql-spec.md)
