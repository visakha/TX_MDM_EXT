# Verification - extract_20261127_005124.zip vs USE_CASES.md (2026-11-27 ~00:58 CST)

Input: ext-docs/extract_20261127_005124.zip -> ZLV_MDM_EVENTS_STORE.dsv (pipe, header, no quotes).

## Counts
- 44 rows, 36 distinct ID01. 5 ID01s repeat (3x, 3x, 3x, 2x, 2x) = 8 extra rows; every repeat carries an
  IDENTICAL payload. Dedupe on ID01, keep one -> 36 events.
- 5 of the 36 are the older samples (JackT 0180, Henry 0190, 0171 x2, 0160) still in the table.
- 31 events from the 2026-11-27 test run. USE_CASES.md expected 26 to about 31 -> top of the range, PASS.
  - 9 CREATE (MASTER_CREATED): all 9 test guests, right source on each xref (zlv_ttf / zlv_eci / c360.default.system). PASS
  - 12 UPDATE: UC8 x2, UC5 x2, UC17, UC16, UC16a, UC19, UC20, UC21, UC22, UC26. PASS
  - 3 MERGE -> 6 events: survivor = MASTER_UPDATED / MERGE with mergedBusinessIds=[loser] and the loser xref
    membershipChange ADDED; loser = MASTER_MERGED / MERGE with activeBusinessId = survivor, no xrefs.
  - 2 UNMERGE -> 4 events: master that lost a member = MASTER_UPDATED / UNMERGE, xref membershipChange REMOVED;
    re-born master = MASTER_UNMERGED / UNMERGE.

## Open questions from USE_CASES.md - answered by the data
- UC26 (update only the eci member of merged 184): event businessId = 184 (the master); the affected xref
  still says businessId 164 (the source record's original id). changedFields [] and dataDiff [] - the master
  did not change, so survivorship kept the ttf value. History must key the xref by source_system+source_pkey,
  never by the xref's businessId.
- UC13-ttf: the event on 184 marks zlv_eci EVT-UC12C-ECI-1 as REMOVED (not the ttf record). 184 keeps the ttf
  member; 164 gets a MASTER_UNMERGED event. So the event reflects what IDMC actually did, not what was asked.

## Behaviour confirmed (was EXPECTED-UNVERIFIED)
- Child row removed (UC16 phone, UC16a address): dataDiff changeType DELETED per field, oldValue = old, newValue null.
- Field cleared to empty (UC21 last_name): changeType DELETED, newValue null (not empty string).
- Boolean/number set from nothing (UC22): changeType CREATED, newValue true / 1.5 (typed JSON, not text).
- Lookup change (UC20): .Code and .Name each get their own UPDATED entry (Mobile -> Fax).
- Merge survivor with no field change (UC12c) still emits an event with changedFields [].
- Source-driven creates/updates use xrefType DATA; UI edits use PATCH (sourcePKey "ex...").

## Problems found
1. dateOfChange is NULL on 2 events: 193 UNMERGE (MASTER_UPDATED) and 164 MASTER_UNMERGED. Ordering cannot rely
   on dateOfChange alone -> fall back to EVT_DTM.
2. EVT_DTM came out as 1.7958E+12 (sqlplus rounded the number). FIXED in tools/extract/extract.ps1 with
   SET NUMWIDTH 20 (commit 516bc64). Re-run the extract to get full epoch ms.
3. EVT_DTM_TMZ and PROCESSED_DTM are empty on every row (expected: nothing has processed them yet).

## Re-run with full timestamps - extract_20261127_005948.zip (checked ~01:03 CST)
- Same data: 44 rows, 36 distinct ID01.
- EVT_DTM is now full 13-digit epoch ms on every row (NUMWIDTH fix works).
- EVT_DTM is always a little AFTER dateOfChange: 34 to 415 ms later on all 34 events that have both.
  So EVT_DTM is when IDMC wrote the event, dateOfChange is when the record changed.
- The 2 events with no dateOfChange: 193 UNMERGE at EVT_DTM 1795760576313, 164 MASTER_UNMERGED at 1795760582650.
  Both fall in the right place next to their partner events (163 UNMERGED, 184 UNMERGE).
- ORDERING RULE for the app: order by dateOfChange; when it is null use EVT_DTM; tie-break on ID01.
