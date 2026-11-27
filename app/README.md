# tx-mdm-ext app (version 0.1.0)

Spring Boot 4.2 + Spring Data JDBC, Java 21, Maven, Oracle JDBC. A plain `@Scheduled` job
(not Spring Batch) that reads the B360 published events of `zlv_guest` from
`ZLV_MDM_EVENTS_STORE` and builds master and source history in the 9 `EVT_*` tables of
`../ddl/V001__history_tables.sql`.

## What it does

Each pass (default every hour at minute 05, cron in the external file):

1. Optionally starts the IDMC publish job (OFF by default, see below).
2. Reads rows of `ZLV_MDM_EVENTS_STORE` where `PROCESSED_DTM IS NULL` (up to `txmdmext.batch-size`).
3. Dedupes on `ID01` (keeps one row; a copy with a payload wins).
4. Parses `PAYLOAD` into records and enums (eventType, impliedOperation, xrefType,
   xrefChange, membershipChange, changeType). Unknown values map to OTHER / UNKNOWN.
5. Orders by `dateOfChange`, falling back to `EVT_DTM` when it is null, tie-break `ID01`.
6. For each event, in ONE transaction:
   - `EVT_MASTER_HIST`: closes the current row of the master, appends a new one
     (root fields, lookups as `_cd` / `_nm`, states, `_meta`, full payload JSON).
   - `EVT_MASTER_PHONE_HIST`, `EVT_MASTER_SRC_ADDR_HIST`, `EVT_MASTER_ADDR_HIST`,
     `EVT_MASTER_ALT_ID_HIST`: compares the child rows (by `_id`) with the master's previous
     payload. New or changed row = new version. Row gone = final version with `is_deleted = 1`.
   - `EVT_MEMBER_HIST`: `members[]` is the full membership after the event; added members are
     opened, missing ones closed (`id01_closed`). A MASTER_MERGED loser closes all its members.
   - `EVT_FIELD_CHANGE`: one row per `dataDiff` entry (`seq_no` 1..n), values stored as text,
     DELETED entries keep `new_value` null.
   - `EVT_XREF_CHANGE`: one row per `affectedXrefs` entry, keyed by source system + source pkey.
     `business_id` is the master of the event, never the xref's own businessId (UC26).
   - Stamps `PROCESSED_DTM` on every row of that `ID01` in the same transaction, so a crash
     leaves the event unprocessed and history untouched.
7. Idempotent: if `EVT_MASTER_HIST` already holds the `ID01`, nothing is written again, the
   row is only stamped. If an event fails, it is rolled back and later events of the same
   master wait for the next pass, so order is kept.

The history rules above (snapshot diff of children, members[] as the full membership) are
Muse's proposal built on the real 36 events; please check them against your intent.

## What it does NOT do yet

- `EVT_SOURCE_HIST` is NOT filled. It needs source VALUES from the B360 source-record API;
  events only carry source keys. See `source/SourceRecordReader` (interface + TODO).
- The IDMC publish trigger is OFF by default and was never run against your org. It mirrors
  tx-mdm (v3 login, then `POST {mdmUrl}/job/public/api/v1/jobInstance/{kind}` with
  `IDS-SESSION-ID`, body `{"jobDefinitionId": ...}`). The `{kind}` value for a publish job
  is UNVERIFIED (tx-mdm only covers Ingress / Egress / MatchMerge). A 429 stops it, no retry.
- Never ran against Oracle. The SQL column names were checked against V001 by script only.
- It never runs DDL, never drops, truncates or deletes anything (`spring.sql.init.mode=never`,
  no JPA / Hibernate / Flyway / Liquibase). Run `ddl/V001__history_tables.sql` by hand first.

## Build on Windows (PowerShell)

Needs JDK 21 and Maven 3.9+ on PATH.

```powershell
cd C:\path\to\TX_MDM_EXT\app
mvn -B spotless:apply
mvn -B verify
```

`mvn verify` runs Spotless (google-java-format 2.0.0) and Checkstyle (google_checks) at
validate, then compiles, runs the tests and builds `target\tx-mdm-ext-0.1.0.jar`.

Only the tests:

```powershell
mvn -B test
```

## Run

1. Copy `application.properties.example` to a folder OUTSIDE the repo, for example
   `C:\tx-mdm-ext\application.properties`, and fill in the Oracle values.
2. First test: keep `txmdmext.run-once=true` (one pass, then exit).

```powershell
java -jar target\tx-mdm-ext-0.1.0.jar --spring.config.location=file:C:/tx-mdm-ext/application.properties
```

3. For the hourly schedule set `txmdmext.run-once=false` and leave it running.

Log line per pass: rows read, distinct ID01, loaded, already loaded, failed, skipped.

## Layout

| Package | What |
|---|---|
| `event` | Records and enums of the event JSON, parser, dedupe and ordering |
| `history` | Pure Java: payload reader and `HistoryPlanner` (event + prior state -> rows) |
| `store` | Explicit repositories, SQL as named constants (`JdbcClient`) |
| `job` | `HourlyJob` (@Scheduled), `EventProcessor` (one transaction per event), `HistoryWriter` |
| `idmc` | `IdmcPublishTrigger`: disabled default + B360 job call (unverified) |
| `source` | `SourceRecordReader` interface for `EVT_SOURCE_HIST` (TODO) |

Tests (`src/test`) use the real 44 rows / 36 events of `extract_20261127_005948.zip` as
fixtures, no database and no mocking framework.
