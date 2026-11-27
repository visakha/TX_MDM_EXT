# Missing use cases (sample events still needed)

Covered so far (all impliedOperation UPDATE, eventType MASTER_UPDATED):
- UC1-3 root field edits (first_name, clns_first_name)
- UC4 child row edit (x_phone ph_num)
- UC5 child row add (x_src_address CREATED)

Still needed, most important first:

## Record lifecycle
- UC6 CREATE - brand-new guest from the UI (first row in master history).
- UC7 CREATE via ingress - new guest from zlv_ttf (DATA xref, not PATCH).
- UC8 UPDATE via ingress - source record changed by a load, not the UI.
- UC9 DELETE (soft) - master or source record deleted.
- UC10 RESTORE - a deleted record brought back.
- UC11 PURGE - hard delete (admin), if allowed in the org.

## Merge and membership
- UC12 MERGE - two guests merged (mergedBusinessIds filled, membershipChange ADDED, a loser businessId stops being active).
- UC13 UNMERGE - a source pulled out of a master (membershipChange REMOVED, new master born).
- UC14 Match-and-merge job merge - merge done by the batch job, not the UI (userName, correlationId shape).
- UC15 TRUST_OVERRIDE - steward overrides a trusted value.

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
