# Missing use cases (sample events still needed)

Covered so far (all impliedOperation UPDATE, eventType MASTER_UPDATED):
- UC1-3 root field edits (first_name, clns_first_name)
- UC4 child row edit (x_phone phone_num; originally tested on legacy ph_num)
- UC5 child row add (x_src_address CREATED)

Sources: every source-driven case below is run TWICE - once for zlv_ttf and once for zlv_eci (internal IDs, all lowercase; confirmed in the BE zip). UI edits land on c360.default.system (PATCH xref) and are listed once. Label samples like UC7-ttf, UC7-eci.

Still needed, most important first:

## Record lifecycle
- UC6 CREATE - brand-new guest from the UI (first row in master history).
- UC7 CREATE via ingress - new guest from a source (DATA xref, not PATCH). [zlv_ttf, zlv_eci]
- UC8 UPDATE via ingress - source record changed by a load, not the UI. [zlv_ttf, zlv_eci]
- UC9 DELETE (soft) - master or source record deleted. [UI, zlv_ttf, zlv_eci]
- UC10 RESTORE - a deleted record brought back. [UI, zlv_ttf, zlv_eci]
- UC11 PURGE - hard delete (admin), if allowed in the org.

## Merge and membership
- UC12 MERGE - two guests merged. Cover ttf+ttf, eci+eci and ttf+eci (mergedBusinessIds filled, membershipChange ADDED, a loser businessId stops being active).
- UC13 UNMERGE - a source pulled out of a master [zlv_ttf member, zlv_eci member] (membershipChange REMOVED, new master born).
- UC14 Match-and-merge job merge - merge done by the batch job, not the UI, across zlv_ttf and zlv_eci (userName, correlationId shape).
- UC15 TRUST_OVERRIDE - steward overrides a trusted value.

## Cross-source
- UC26 Same guest in both sources - zlv_ttf and zlv_eci both contribute to one master; then update only the eci side and check which source the history credits.
- UC27 One source drops a value the other still supplies (survivorship switches source).

## Child rows
- UC16 Child row delete - remove a phone (x_phone DELETED).
- UC17 Several child changes in one save - e.g. add one phone, edit another.
- UC18 OOTB Address group add/edit (Address, not x_src_address).
- UC19 x_alt_id add/edit (date field start_date).
- UC20 Lookup change - change ph_typ or state_cd (Code/Name pair).

## Field edge cases
- UC21 Set a field to empty (clear a value: newValue missing/null).
- UC22 Boolean and number fields - do_not_contact_ind, touch_incr, last_touch_dtm.

## Validation
- UC23 VALIDATE - record goes to validation FAILED (states.validation), e.g. junk phone.

## Platform behaviour
- UC24 Same event published twice (already seen - dedupe on ID01).
- UC25 Many events for one master in one hour - order by dateOfChange.
