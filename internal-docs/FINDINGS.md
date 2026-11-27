# Findings from ext-docs (2026-11-27)

Read-only review of: Exp_001.dsv.csv, Ora_pub_table_ddl.txt, pub_evt_zlv_guest__schema_002.json, ZLV Guest-1795731199344.zip.

## Event store (Ora_pub_table_ddl.txt)
- ZLV_MDM_EVENTS_STORE: id01 VARCHAR2(255), evt_dtm NUMBER(19) (epoch ms), evt_dtm_tmz TIMESTAMP, payload CLOB, processed_dtm TIMESTAMP.
- processed_dtm already exists - that is our processed marker.
- NOTE: the file starts with "DROP table ZLV_MDM_EVENTS_STORE;". That is Vamsi's own script; nothing in this repo will carry a DROP.

## Extract (Exp_001.dsv.csv)
- 8 rows, 5 distinct ID01. 3 ID01s appear twice (repeat publish runs). Dedupe on ID01 -> 5 events.
- evt_dtm is filled (epoch ms); evt_dtm_tmz is blank.
- The 5 events match the use cases:
  - JackT: BID 100000000000180, first_name Jack -> JackT
  - Henry: BID 100000000000190, first_name Henry -> Henry T3
  - BID 100000000000171, clns_first_name what -> y this kolavaridhi
  - UC4 BID 100000000000171, x_phone[d7be8f...].ph_num 12334455 -> 12334455001
  - UC5 BID 100000000000160, x_src_address[8ca983...] CREATED (13 field diffs)
- All are eventType MASTER_UPDATED, impliedOperation UPDATE, user VVamsi-idmcdev.

## Event JSON (schema 002)
- Root: businessId, activeBusinessId, assetId (zlv_guest), eventType, impliedOperation, dateOfChange (epoch ms), correlationId, userName, sourceSystem, states {base, validation, createMode}, changedFields[], mergedBusinessIds[].
- payload = master record AFTER the change. Null fields are left out, so it is sparse.
- dataDiff[] = field-level old/new. fieldPath carries the child row _id, e.g. x_phone[<_id>].ph_num.
- members[] = sourceSystemId + sourcePKey + businessId of every source record in the master.
- affectedXrefs[] = which source record changed (xrefType DATA/PATCH/DELETION/ANCHOR, xrefChange, membershipChange ADDED/REMOVED/NONE).
- UI edits land on a PATCH xref (sourcePKey starts with "ex"). Source field VALUES are not in the event; source history from events alone = membership + xref change only.

## Business entity zlv_guest (zip)
- Root fields: first_name, clns_first_name, last_name, clns_last_name, ffr_first_name, ffr_last_name (text 255), do_not_contact_ind (bool), linkedin_url, last_touch_dtm, prev_last_touch_dtm (datetime), touch_incr (double), touch_notes (text), touch_cd (lookup).
- Many-valued groups (each child row has _id): x_phone (cntry_cd int, ph_num int, ph_typ lookup, no_mrkt_calls bool); Address (OOTB, 23 fields); x_src_address (13 fields, postal_cd int); x_alt_id (7 fields, start_date date).
- Lookups arrive as {Code, Name}.

## Proposed tables (not written yet)
- EVT_MASTER_HIST: one row per event per master - root fields flattened, states, event metadata, payload snapshot CLOB, valid_from / valid_to (Type 2).
- EVT_MASTER_PHONE_HIST, EVT_MASTER_SRC_ADDR_HIST, EVT_MASTER_ADDR_HIST, EVT_MASTER_ALT_ID_HIST: child rows keyed by business_id + child _id, Type 2.
- EVT_FIELD_CHANGE: one row per dataDiff entry (field path, child _id, change type, old, new).
- EVT_MEMBER_HIST: master-source membership, Type 2.
- EVT_XREF_CHANGE: one row per affectedXref per event.
