# tx-mdm-ext app (version 0.1.0)

Spring Boot 4.1.2 + Spring Data JDBC, Java 21, Maven, Oracle JDBC. A plain scheduled job
(not Spring Batch) that reads the B360 published events of `zlv_guest` from
`ZLV_MDM_EVENTS_STORE` and builds master and source history in the 9 `EVT_ZLV_GUEST_*`
tables of `../ddl/V001__history_tables.sql` (fresh schema) / `../ddl/V002__zlv_guest_tables.sql`.

## History tables: EVT_ZLV_GUEST_*

Every history table carries its domain prefix, so other domains (person, provider, ...) can get
their own tables later: `EVT_ZLV_GUEST_MASTER_HIST`, `_MASTER_PHONE_HIST`, `_MASTER_SRC_ADDR_HIST`,
`_MASTER_ADDR_HIST`, `_MASTER_ALT_ID_HIST`, `_FIELD_CHANGE`, `_MEMBER_HIST`, `_XREF_CHANGE`,
`_SOURCE_HIST`. Constraint and index names carry the prefix too (`pk_evt_zlv_guest_...`).

Some names are longer than 30 characters (longest `EVT_ZLV_GUEST_MASTER_SRC_ADDR_HIST`, 34).
That needs Oracle 12.2+ with `COMPATIBLE >= 12.2` (identifiers up to 128 bytes).

Which DDL to run (by hand; CREATE only, nothing is ever dropped):

- Your database (already ran the FIRST V001 with unprefixed `EVT_*` tables): run
  `ddl/V002__zlv_guest_tables.sql` once. It creates the 9 prefixed tables with `row_md5`.
  The old unprefixed `EVT_*` tables are left untouched and empty; the app no longer writes
  to them. Removing them is your / the DBA's call, by hand.
- A fresh schema: run `ddl/V001__history_tables.sql` (same 9 tables). Do not run both.
- Then, in EITHER case, run `ddl/V003__zlv_guest_phone_num.sql` once. It only ADDs the
  text column `phone_num` to `EVT_ZLV_GUEST_MASTER_PHONE_HIST`. The app reads and writes
  `phone_num` (B360 x_phone.phone_num, TEXT); the old Integer `ph_num` column stays, unused.

## Package layout: generic engine + one package per model

Everything that names a zlv_guest field or table lives in `com.visakha.txmdmext.model.zlvguest`:
the root and child-group records, the payload reader, the hash input, the table names
(`GuestTables.PREFIX = "EVT_ZLV_GUEST_"`) and the two repositories whose SQL lists guest
columns. The engine (event parsing, dedupe, ordering, Type 2 open/close, row_md5,
field-change, xref, membership, job, config, the generic repositories) sits in the shared
packages and only talks to the small `model.EntityModel` interface plus `store.MasterStore` /
`store.ChildStore`. Another model later is a sibling package (for example `model.person`)
with its own tables; a unit test checks that no engine file names a guest field or table.

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
| `txmdmext.idmc.*` | Publish-job kick-off: job `mdm_zlv_guest_pub_evts_001`, Job Process Type `Publish Business Events`. |
| `server.port` | Port of the heartbeat endpoint (`8085` in the example). |
| `txmdmext.log-dir` | Folder for the log file, see Log file location. |

The publish trigger (when enabled) runs at the start of the same pass, so there is only one
interval. The next pass starts `txmdmext.scan.interval` after the previous one finished.

## Change data capture: row_md5

The 7 Type 2 tables (`EVT_ZLV_GUEST_MASTER_HIST`, `_MASTER_PHONE_HIST`, `_MASTER_SRC_ADDR_HIST`,
`_MASTER_ADDR_HIST`, `_MASTER_ALT_ID_HIST`, `_MEMBER_HIST`, `_SOURCE_HIST`) carry
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

Both V001 (fresh schema) and V002 (your database) create the tables with `row_md5` built in.

## What it does

Each pass:

1. Starts the IDMC publish job `mdm_zlv_guest_pub_evts_001` when `txmdmext.idmc.enabled=true`.
2. Reads rows of `ZLV_MDM_EVENTS_STORE` where `PROCESSED_DTM IS NULL` (up to `txmdmext.batch-size`).
3. Dedupes on `ID01` (keeps one row; a copy with a payload wins).
4. Parses `PAYLOAD` into records and enums (eventType, impliedOperation, xrefType,
   xrefChange, membershipChange, changeType). Unknown values map to OTHER / UNKNOWN.
5. Orders by `dateOfChange`, falling back to `EVT_DTM` when it is null, tie-break `ID01`.
6. For each event, in ONE transaction:
   - `EVT_ZLV_GUEST_MASTER_HIST`: if the row_md5 changed, closes the current row and appends a new one
     (root fields, lookups as `_cd` / `_nm`, states, `_meta`, full payload JSON).
   - the four child tables (`..._PHONE_HIST`, `..._SRC_ADDR_HIST`, `..._ADDR_HIST`,
     `..._ALT_ID_HIST`): compares each child row (by `_id`) with the open row in the
     table by row_md5. New or changed = new version. Gone = final version with `is_deleted = 1`.
   - `EVT_ZLV_GUEST_MEMBER_HIST`: `members[]` is the full membership after the event; added members are
     opened, missing ones closed (`id01_closed`). A MASTER_MERGED loser closes all its members.
   - `EVT_ZLV_GUEST_FIELD_CHANGE`: one row per `dataDiff` entry (`seq_no` 1..n), values stored as text,
     DELETED entries keep `new_value` null. Every `touch_notes` edit is its own row.
   - `EVT_ZLV_GUEST_XREF_CHANGE`: one row per `affectedXrefs` entry, keyed by source system + source pkey.
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

- `EVT_ZLV_GUEST_SOURCE_HIST` is NOT filled. It needs source VALUES from the B360 source-record API;
  events only carry source keys. See `source/SourceRecordReader` (interface + TODO).
- Publish trigger: Vamsi tested the trigger and job type in his org on 2026-11-26 afternoon and
  they work. Job Name `mdm_zlv_guest_pub_evts_001`, Job Process Type `Publish Business Events`.
  The app calls v3 login, then `POST {mdmUrl}/job/public/api/v1/jobInstance/{kind}` with
  `IDS-SESSION-ID`, body `{"jobDefinitionId": ...}`. Put the `{kind}` value and the job
  definition ID from your tested call into application.properties; the app does not look the
  ID up from the job name. A 429 stops it, no retry. The password is never logged.
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
| `it/H2RerunIT` | second run adds nothing, reprocessing adds nothing, bad rows, V002 beside the old tables |
| `model/zlvguest/fileimport/FileImportGeneratorTest` | file import generator: same seed same files, ASCII / no quotes / CRLF, files match expected, round 3 one delete per row, lookups in LOOKUPS.md, dates increase |
| `model/zlvguest/ZlvGuestModelTest` | prefix on all 9 tables, tables in both DDL files, hash columns, engine stays guest-free |
| `store/SqlNamesTest` | only plain Oracle identifiers get into SQL text |
| `web/HeartbeatTest` | heartbeat built from the scan status; GET /api/heartbeat through MockMvc |

The H2 tests create a fresh in-memory database per test class and apply
`../ddl/V001__history_tables.sql` unchanged plus a test-only CREATE of
`ZLV_MDM_EVENTS_STORE` (`src/test/resources/db/`). Nothing is dropped or deleted, even there.
H2 is a test-scope dependency only.

## File import test generator

Seeded random test files for the Business 360 File Import of zlv_guest, three rounds on the
same guests, as in `internal-docs/file-import/` (see `FILE_IMPORT_USER_GUIDE.md` there):

- round 1: creates. N guests, each with 0..3 phones, 0..3 addresses, 0..3 alt ids
  (cross-product rows).
- round 2: in-place adds and edits on the same guests (phone type, zip), no deletes.
- round 3: deletes only, one deleted entry per row, root `Operation Type` = `Patch` with the
  guest's current root values, the group's `Operation Type` = `Delete`.
- per round an `_expected.csv`: per guest the child keys, counts and values after that round,
  to check the publish-events extract against.

Plain Java main (`model.zlvguest.fileimport.FileImportGeneratorMain`): no Spring, no database,
no scan job. Same seed and options = the same files. Files are never overwritten; a second
run into the same folder stops with an error.

```powershell
cd C:\path\to\TX_MDM_EXT\app
mvn -B -DskipTests package
.\generate-file-import.ps1 -Seed 42 -Guests 5 -OutDir C:\tx-mdm-ext\file-import
```

Same thing without the script:

```powershell
java -cp target\classes com.visakha.txmdmext.model.zlvguest.fileimport.FileImportGeneratorMain --seed=42 --guests=5 --rounds=3 --base-date=2026-11-01 --out-dir=C:\tx-mdm-ext\file-import
```

| Option | Default | What |
|---|---|---|
| `--seed` | 42 | Random seed; also the key prefix `RG-<seed>-<n>` (no clash with `FI-UC-*`) |
| `--guests` | 5 | 1..500 |
| `--rounds` | 3 | 1..3 |
| `--base-date` | 2026-11-01 | Round 1 dates fall in the 3 years before it; round 2 = +1 day, round 3 = +2 days |
| `--out-dir` | `file-import-out` | Files go to `<out-dir>\RG-<seed>\` |

Output: `zlv_guest_RG-<seed>_round<n>.csv` and `zlv_guest_RG-<seed>_round<n>_expected.csv`.
All files use the same 32-column header (root `Operation Type` first), so one saved wizard
mapping fits every round. Rounds 1 and 2 leave every Operation Type blank (the wizard choice
applies, as in the verified rounds); only round 3 fills them.

## Layout

| Package | What |
|---|---|
| `config` | `AppProperties` (all `txmdmext.*` settings), bean wiring |
| `model` | `EntityModel`, `ChildGroup`, `ChildRow`, `EntityRoot`: what the engine needs from a model |
| `model.zlvguest` | zlv_guest only: records, payload reader, table names, hash input, master/child repositories |
| `model.zlvguest.fileimport` | File import test generator (plain main, no Spring) |
| `event` | Records and enums of the event JSON, parser, dedupe and ordering |
| `history` | Pure Java: `HistoryPlanner` (event + prior state -> rows), `RowHash` (row_md5) |
| `store` | Event store, field change, membership/xref, idempotency repositories (`JdbcClient`); `MasterStore` / `ChildStore` interfaces |
| `job` | `ScanJob` (interval from properties), `EventProcessor` (one transaction per event), `HistoryWriter` |
| `idmc` | `IdmcPublishTrigger`: B360 publish job call (`mdm_zlv_guest_pub_evts_001`) |
| `source` | `SourceRecordReader` interface for `EVT_ZLV_GUEST_SOURCE_HIST` (TODO) |

## How SQL gets its table names

SQL stays as named text-block constants. Guest repositories fill in their table name once, from
the `GuestTables` constants (`"""...FROM %s...""".formatted(GuestTables.MASTER)`). The shared
repositories (field change, membership, xref, idempotency) take the names from `EntityModel` at
startup and check each with `SqlNames.table` (plain `A-Z 0-9 _` identifier, max 128). Names come
from code constants, never from input or properties. No reflection, no ORM.

## Log file location

The log folder is configurable in your external application.properties:

    txmdmext.log-dir=C:/tx-mdm-ext/logs
    logging.file.name=${txmdmext.log-dir}/tx-mdm-ext.log

Point txmdmext.log-dir at any folder you like; the log is written to tx-mdm-ext.log in that
folder (rolled at 10 MB, 30 files kept). Use forward slashes on Windows. You can also
override it for one run on the command line: --txmdmext.log-dir=D:/somewhere/logs

## Heartbeat: GET /api/heartbeat

A small read-only endpoint that says the app is up and when the last scan pass ran. It is
built from memory (no database call), so it answers even while Oracle is down.

```powershell
Invoke-RestMethod http://localhost:8085/api/heartbeat
```

```bash
curl http://localhost:8085/api/heartbeat
```

Example answer:

```json
{"status":"UP","app":"tx-mdm-ext","version":"0.1.0",
 "startedAt":"2026-11-27T08:00:00Z","now":"2026-11-27T09:05:00Z","uptimeSeconds":3900,
 "lastScanStartedAt":"2026-11-27T09:00:00Z","lastScanEndedAt":"2026-11-27T09:00:04Z",
 "lastScanResult":{"rowsRead":44,"distinctEvents":36,"loaded":36,"alreadyLoaded":0,"failed":0,"skipped":0},
 "lastScanError":null,"nextScanDueAt":"2026-11-27T10:00:04Z","scanInterval":"PT1H"}
```

- `lastScanResult`, `lastScanStartedAt`, `lastScanEndedAt` are null until the first pass ends.
- `lastScanError` holds the error of the last pass if it failed, else null.
- `nextScanDueAt` is null in run-once mode.
- Port: `server.port` in the external application.properties.
- `txmdmext.run-once=true`: the app exits after its one pass, so the heartbeat is only up for
  that pass. `txmdmext.run-once=false`: the app stays up and the heartbeat answers until you
  stop it.
- The state is in memory only; after a restart it starts empty again.
