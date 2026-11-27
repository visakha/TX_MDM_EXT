# tx-mdm-ext app (version 0.1.0)

Spring Boot 4.1.2 + Spring Data JDBC, Java 21, Maven, Oracle JDBC. A plain scheduled job
(not Spring Batch) that reads the B360 published events of `zlv_guest` from
`ZLV_MDM_EVENTS_STORE` and builds master and source history in the 9 `EVT_*` tables of
`../ddl/V001__history_tables.sql`.

## Configuration: application.properties

The app reads ONE external file, named exactly `application.properties`, kept OUTSIDE the
repo. The repo ships `application.properties.example` with placeholders only. Copy it,
rename it to `application.properties`, fill in the Oracle values. No secrets in the repo.

| Property | What |
|---|---|
| `txmdmext.scan.interval` | Time between passes, a Spring Duration: `1m`, `5m`, `10m`, `1h`, `2h`. Required, no default in code. |
| `txmdmext.run-once` | `true` = one pass, then exit (first tests). `false` = keep running on the interval. |
| `txmdmext.batch-size` | Max unprocessed rows read per pass. |
| `txmdmext.zone` | Zone for the timestamps written (`UTC`). |
| `txmdmext.md5.<TABLE>` | row_md5 formula per Type 2 table, see below. All 7 required. |
| `txmdmext.idmc.*` | Publish-job kick-off, `enabled=false` by default. |

The publish trigger (when enabled) runs at the start of the same pass, so there is only one
interval. The next pass starts `txmdmext.scan.interval` after the previous one finished.

## Change data capture: row_md5

The 7 Type 2 tables (`EVT_MASTER_HIST`, `EVT_MASTER_PHONE_HIST`, `EVT_MASTER_SRC_ADDR_HIST`,
`EVT_MASTER_ADDR_HIST`, `EVT_MASTER_ALT_ID_HIST`, `EVT_MEMBER_HIST`, `EVT_SOURCE_HIST`) carry
`row_md5 CHAR(32)`. The formula of each table is the ordered column list in
`txmdmext.md5.<TABLE>`:

    row_md5 = md5( norm(col1) + "|" + norm(col2) + "|" + ... )   lowercase hex

- `norm`: null -> `\N`; text trimmed, `\` and `|` escaped; boolean -> `1` / `0`; numbers as
  plain decimals without trailing zeros; timestamps ISO-8601 UTC; dates ISO (`2026-11-27`).
- Order matters. Only listed columns count.
- A new version is written only when the new row_md5 differs from the current row.
  Example: UC26 (empty dataDiff, same content) writes no new master version.
- Columns are checked at startup: an unknown column, an unknown table, an empty list or a
  missing table entry stops the app with a clear message. There is no silent default.
- The default master list leaves out event metadata (event type, user, date of change,
  correlation id, `_meta`), so an event that changes no content writes no version.
- Changing a list changes all future hashes; old rows keep their old hashes, so expect one
  extra version per record after a change.

DDL: `ddl/V001__history_tables.sql` now has `row_md5` built in (for a fresh schema).
You already ran the old V001, so run `ddl/V002__add_row_md5.sql` (ALTER TABLE ... ADD only)
once by hand. Existing rows keep `row_md5` NULL; the next event of each record writes one
version with a hash.

## What it does

Each pass:

1. Optionally starts the IDMC publish job (OFF by default, see below).
2. Reads rows of `ZLV_MDM_EVENTS_STORE` where `PROCESSED_DTM IS NULL` (up to `txmdmext.batch-size`).
3. Dedupes on `ID01` (keeps one row; a copy with a payload wins).
4. Parses `PAYLOAD` into records and enums (eventType, impliedOperation, xrefType,
   xrefChange, membershipChange, changeType). Unknown values map to OTHER / UNKNOWN.
5. Orders by `dateOfChange`, falling back to `EVT_DTM` when it is null, tie-break `ID01`.
6. For each event, in ONE transaction:
   - `EVT_MASTER_HIST`: if the row_md5 changed, closes the current row and appends a new one
     (root fields, lookups as `_cd` / `_nm`, states, `_meta`, full payload JSON).
   - `EVT_MASTER_PHONE_HIST`, `EVT_MASTER_SRC_ADDR_HIST`, `EVT_MASTER_ADDR_HIST`,
     `EVT_MASTER_ALT_ID_HIST`: compares each child row (by `_id`) with the open row in the
     table by row_md5. New or changed = new version. Gone = final version with `is_deleted = 1`.
   - `EVT_MEMBER_HIST`: `members[]` is the full membership after the event; added members are
     opened, missing ones closed (`id01_closed`). A MASTER_MERGED loser closes all its members.
   - `EVT_FIELD_CHANGE`: one row per `dataDiff` entry (`seq_no` 1..n), values stored as text,
     DELETED entries keep `new_value` null. Every `touch_notes` edit is its own row.
   - `EVT_XREF_CHANGE`: one row per `affectedXrefs` entry, keyed by source system + source pkey.
     `business_id` is the master of the event, never the xref's own businessId (UC26).
   - Stamps `PROCESSED_DTM` on every row of that `ID01` in the same transaction, so a crash
     leaves the event unprocessed and history untouched.
7. Idempotent: if any history table already holds the `ID01`, nothing is written again, the
   row is only stamped.
8. Bad input: an empty or invalid JSON payload is logged with its ID01, listed as failed in
   the pass summary and left unprocessed (it shows up again next pass). The batch goes on.
   If an event fails to load it is rolled back and later events of the same master wait for
   the next pass, so order is kept.

The history rules above (members[] as the full membership, child compare by `_id`) are
Muse's proposal built on the real 36 events; please check them against your intent.

## What it does NOT do yet

- `EVT_SOURCE_HIST` is NOT filled. It needs source VALUES from the B360 source-record API;
  events only carry source keys. See `source/SourceRecordReader` (interface + TODO).
- The IDMC publish trigger is OFF by default and was never run against your org. It mirrors
  tx-mdm (v3 login, then `POST {mdmUrl}/job/public/api/v1/jobInstance/{kind}` with
  `IDS-SESSION-ID`, body `{"jobDefinitionId": ...}`). The `{kind}` value for a publish job
  is UNVERIFIED. A 429 stops it, no retry. The password is never logged.
- Never ran against Oracle. Tested on H2 in Oracle mode (see Tests).
- It never runs DDL, never drops, truncates or deletes anything (`spring.sql.init.mode=never`,
  no JPA / Hibernate / Flyway / Liquibase).

## Build on Windows (PowerShell)

Needs JDK 21 and Maven 3.9+ on PATH.

```powershell
cd C:\path\to\TX_MDM_EXT\app
mvn -B spotless:apply
mvn -B verify
```

`mvn verify` runs Spotless (google-java-format 2.0.0) and Checkstyle (google_checks) at
validate, compiles, runs the unit tests (`*Test`, surefire), then the H2 integration tests
(`*IT`, failsafe), and builds `target\tx-mdm-ext-0.1.0.jar`.

Only the unit tests:

```powershell
mvn -B test
```

## Run

1. Copy `application.properties.example` to a folder OUTSIDE the repo as
   `C:\tx-mdm-ext\application.properties` and fill in the Oracle values.
2. First test: keep `txmdmext.run-once=true` (one pass, then exit).

```powershell
java -jar target\tx-mdm-ext-0.1.0.jar --spring.config.location=file:C:/tx-mdm-ext/application.properties
```

3. For the schedule set `txmdmext.run-once=false` and `txmdmext.scan.interval` (for example
   `1h`), and leave it running.

Log line per pass: rows read, distinct ID01, loaded, already loaded, failed ID01s, skipped.

## Tests

No mocking framework. Fixtures are the real 44 rows / 36 events of
`extract_20261127_005948.zip` (`src/test/resources/events/`).

| Class | What |
|---|---|
| `EventParserTest` | parsing, sparse payloads, empty / invalid / non-object JSON |
| `EventBatchTest` | dedupe (identical duplicates kept once), ordering |
| `RowHashTest` | MD5: stable, known value, order, nulls, normalizing, fail fast |
| `AppPropertiesTest` | scan interval 1m 5m 10m 1h 2h, missing interval, example file |
| `HistoryPlannerTest` | general history rules, no version for unchanged content, touch_notes |
| `UseCaseTest` | one test per use case of USE_CASES.md and the 5 older samples |
| `it/H2HistoryIT` | end to end on H2 (MODE=Oracle): real V001, real SQL, real rows checked |
| `it/H2RerunIT` | second run adds nothing, reprocessing adds nothing, bad rows, V002 |

The H2 tests create a fresh in-memory database per test class and apply
`../ddl/V001__history_tables.sql` unchanged plus a test-only CREATE of
`ZLV_MDM_EVENTS_STORE` (`src/test/resources/db/`). Nothing is dropped or deleted, even there.
H2 is a test-scope dependency only.

## Layout

| Package | What |
|---|---|
| `config` | `AppProperties` (all `txmdmext.*` settings), bean wiring |
| `event` | Records and enums of the event JSON, parser, dedupe and ordering |
| `history` | Pure Java: `HistoryPlanner` (event + prior state -> rows), `RowHash` (row_md5) |
| `store` | Explicit repositories, SQL as named constants (`JdbcClient`) |
| `job` | `ScanJob` (interval from properties), `EventProcessor` (one transaction per event), `HistoryWriter` |
| `idmc` | `IdmcPublishTrigger`: disabled default + B360 job call (unverified) |
| `source` | `SourceRecordReader` interface for `EVT_SOURCE_HIST` (TODO) |
