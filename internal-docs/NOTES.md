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
