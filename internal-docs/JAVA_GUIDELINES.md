# Java development guidelines - TX_MDM_EXT

Keep it simple, modern and readable. A mid-level engineer should follow every line.

## Language
- Records and stream processing are the default style (Vamsi, 2026-11-26).
- Use the latest Java language features: records, switch expressions, pattern matching for instanceof and switch, sealed interfaces, text blocks, var where the type is obvious, lambdas and streams.
- Prefer enums over string constants.
- Records for DTOs and value objects.
- Sequential streams only. No multi-threading unless we agree on it.
- No reflection tricks, no clever generics.

## Spring
- Spring Boot + Spring Data JDBC. No JPA, no Hibernate.
- Explicit repositories. SQL as named constants, not string-built.
- Constructor injection only.
- Plain @Scheduled job unless we decide on Spring Batch.

## External configuration
- Everything that can change lives in an external application.properties, outside the jar: schedule interval or cron, database URL and user, IDMC settings, batch sizes.
- The app reads it with --spring.config.location (or spring.config.import). The repo carries only an application.properties.example with placeholders.
- No passwords in the repo. Real values come from the external file or environment variables on the box that runs it.

## NEVER DROP TABLES (most important rule)
- No DDL in this repo ever drops or truncates a table. No DROP, no TRUNCATE, no DELETE-all scripts.
- DDL is CREATE only, plus ALTER ... ADD for new columns.
- Nothing auto-generates or recreates schema: no Hibernate/JPA ddl-auto (we do not use JPA at all), spring.sql.init.mode=never, no Flyway/Liquibase clean.
- Vamsi or the DBA runs the DDL by hand. The app never runs DDL.

## Data
- Oracle is the store. DDL lives in versioned SQL files in the repo.
- History is append and close only. Never rewrite past history rows.

## Tests
- Only the tests we really need. No overly complicated unit tests.
- No mocking framework. Plain sample data is fine.
- Real JSON samples from ext-docs/ are the test fixtures.

## Comments
- Short and plain. Explain why, not what. Three lines at most.
- No overly complicated comments.

## Style
- ASCII only in code, logs, output and docs. No icons or special unicode.
- Google Java Style, enforced by the build (Spotless + Checkstyle).
