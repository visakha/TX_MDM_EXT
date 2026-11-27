# Working notes

Muse's working notes for TX_MDM_EXT. Decisions, what is proven, what is next.

- 2026-11-26: repo created. Context and Java guidelines written. Waiting on the three inputs in ext-docs/.
- 2026-11-26: processed marker = TIMESTAMP column on ZLV_MDM_EVENTS_STORE (Vamsi chose timestamp over flag).
- 2026-11-26: sample event use cases Vamsi is generating in IDMC (zlv_guest) for ext-docs/:
  - UC1-UC3: simple root-level edits - cleanse first name / first name.
  - UC4: business ID ending 0171 - edited an existing phone number (phone field group).
  - UC5: business ID ending 0160 - added an address (address field group).
  He runs the publish job himself to land these in ZLV_MDM_EVENTS_STORE, then uploads the JSON.
- 2026-11-26 23:52: publish run returned 8 rows (3 earlier rows kept from repeated Custom runs + 5 new). Vamsi is switching the publish job to STANDARD mode from now on.
- Dedupe rule (Vamsi): the same event can land more than once in ZLV_MDM_EVENTS_STORE when the publish window overlaps. Dedupe on ID01 - if an ID01 appears more than once, keep one. Applies to the Oracle extract he sends AND to the app's read of the event store.
- 2026-11-27: scan interval is `txmdmext.scan.interval` in the external `application.properties` (Spring Duration: 1m, 5m, 10m, 1h, 2h). Nothing hard-coded. Publish trigger runs on the same pass.
- 2026-11-27: change data capture with `row_md5 CHAR(32)` on the 7 Type 2 tables. Formula per table = ordered column list in `txmdmext.md5.<TABLE>`; values normalized (null -> `\N`, trimmed text with `\` and `|` escaped, booleans 1/0, plain decimals, ISO timestamps/dates), joined with `|`, MD5 lowercase hex. Equal md5 = no new version. Unknown column / table or missing entry fails at startup. Master default list leaves event metadata out, so UC26 (empty diff) writes no master version. (Superseded 01:32: see the EVT_ZLV_GUEST_ entry - the new tables include row_md5, no ALTER file.)
- 2026-11-27: Spring Boot 4.1.2 on Java 21 (Vamsi approved). H2 (MODE=Oracle) integration tests run the real V001 and the real repository SQL on the 44 real rows; every production statement ran on H2 unchanged.
- 2026-11-27 01:32: TABLE NAMING (Vamsi): every history table carries the domain prefix, EVT_ZLV_GUEST_* (EVT_MEMBER_HIST -> EVT_ZLV_GUEST_MEMBER_HIST, all 9). Constraints/indexes too (pk_/uq_/ix_evt_zlv_guest_...), longest name 41 chars. Names over 30 chars need Oracle 12.2+ with COMPATIBLE >= 12.2. V001 = fresh-schema CREATEs with the new names + row_md5; V002__zlv_guest_tables.sql = the same CREATEs for his database, which ran the first V001. The old unprefixed EVT_* tables are left untouched and empty; removal is his/DBA's call by hand. The ALTER-ADD-row_md5 V002 idea is dropped.
- 2026-11-27 01:30: PACKAGE NAMESPACE (Vamsi): zlv_guest lives in com.visakha.txmdmext.model.zlvguest (records, payload reader, GuestTables with PREFIX, hash input, the master/child repositories). The engine only knows model.EntityModel + store.MasterStore/ChildStore. Nothing built for "member" (no specs given). A unit test fails if an engine file names a guest table or field.

