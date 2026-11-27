# Context - TX_MDM_EXT

Status: design only. No code yet. Written 2026-11-26 from Vamsi's voice briefing.

## Why this exists
IDMC MDM SaaS (Business 360) is missing two things:
1. It keeps no history of MASTER data, so you cannot egress the master as it was at any point in time.
2. It keeps no history of SOURCE data changes, so you cannot ingress or egress source data as it was at any point in time.

So we build an external history store ourselves, in Oracle.

## How it works
- MDM publishes business events (create, update, merge, unmerge) for the zlv_guest business entity.
- The publish job "Pub EVT ZLV guest" (CDI mapping task, Business 360 Events source) lands each event as a row in the Oracle table ZLV_MDM_EVENTS_STORE.
- A Spring Boot + Spring Data JDBC app, once an hour:
  1. kicks off the publish job,
  2. picks up the new rows in ZLV_MDM_EVENTS_STORE,
  3. parses the PAYLOAD column (JSON),
  4. writes master data history and source data history tables.
- The history tables live in the SAME Oracle schema as ZLV_MDM_EVENTS_STORE.

## ZLV_MDM_EVENTS_STORE (as described, to be confirmed from DDL)
- ID01
- EVENT_DTM (blank today, to be filled later)
- EVENT_DTM with time zone (blank today, to be filled later)
- PAYLOAD - JSON, has a JSON schema
- A new column is to be added (likely a processed marker) - to confirm.

## What was proven on 2026-11-26
- The publish job works end to end into Oracle. A Custom-window run returned 2 rows (edits to JackT and Henry) and both landed correctly.
- Standard publish mode only picks up events since the last run. An early empty run makes later runs look empty. Use Custom with a window covering the edit times (IDMC screens show Pacific time) when testing.
- Snowflake as a target is blocked only by the role lacking temp-table rights. Oracle is the target for now.

## Inputs coming into ext-docs/
1. JSON schema of the event PAYLOAD.
2. Real event JSON instances.
3. Business entity definition (zip).

## Deliverables
1. Oracle DDL for master history and source history tables (designed from the three inputs).
2. Spring Boot app that fills those tables every time new rows appear in ZLV_MDM_EVENTS_STORE.

## Open questions
- Plain Spring Boot @Scheduled (recommended at one batch an hour) or Spring Batch.
- How the app remembers which event rows it already processed (processed marker column vs watermark table).
- History shape: Type 2 (valid from / valid to) for master, source, and master-source membership.
- Publish mode for the hourly kick-off: Standard vs Custom window.
- How the app signs in to IDMC to start the publish job (credentials stay out of the repo).
