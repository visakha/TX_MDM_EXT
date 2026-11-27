# Java development guidelines - TX_MDM_EXT

Keep it simple, modern and readable. A mid-level engineer should follow every line.

## Language
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
