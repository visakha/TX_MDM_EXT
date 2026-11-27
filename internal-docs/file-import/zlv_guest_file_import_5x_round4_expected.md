# Round 4 - expected results (all EXPECTED-UNVERIFIED until the extract comes back)

Two files, because the source system is chosen in the wizard for the whole file.

| File | Wizard source | Wizard mode |
|---|---|---|
| zlv_guest_file_import_5x_round4a.csv (5 rows) | zlv_ttf | Update record sections (Patch). Do NOT pick "Update only fields containing data" - case R4-1 needs the blank to count. |
| zlv_guest_file_import_5x_round4b_zlv_eci.csv (1 row) | zlv_eci | Create or overwrite records (Upsert) |

Order: upload 4a, then 4b, then run the publish job once and extract.
Optional R4-7: after that publish, upload 4a a second time unchanged, publish again.

| Case | Guest | Row does | Outcome A (one of these) | Outcome B |
|---|---|---|---|---|
| R4-1 blank cell | FI-UC-1 (BID 200) | Patch root, last_name empty | MASTER_UPDATED, dataDiff last_name DELETED (Phonesonly -> null) | last_name kept (blank ignored), only touch_notes UPDATED |
| R4-2 stale date | FI-UC-2 (BID 201) | Source Last Update Date 2026-01-01, older than R2 (2026-11-26 10:00); last_name -> Addronlystale | Applied anyway: MASTER_UPDATED last_name + touch_notes | Skipped as stale: no event, last_name stays Addronly |
| R4-3 bad lookup | FI-UC-3 (BID 202) | new phone PH-9 with ph_typ "Mobil" (not on the list) | Row rejected (import shows failed 1), no event, phones still PH-1 + PH-2 | Phone added with empty type (would be a finding) |
| R4-4 too-long number | FI-UC-4 (BID 203) | new phone PH-8, phone_num 12345678901 (11 digits) | Row rejected "invalid format", no event | Phone added (would contradict round 1 lesson) |
| R4-5 no-op | FI-UC-5 (BID 204) | Patch root with exactly its current values and the same R2 date | No event at all | MASTER_UPDATED with empty dataDiff (means every re-send floods the store) |
| R4-6 second source | FI-UC-1 from zlv_eci | same Source Primary Key FI-UC-1, source zlv_eci, last_name Ecisource, phone FI-UC-1-ECI-PH-1 | New source record + new master: MASTER_CREATED, xref zlv_eci; BID 200 (zlv_ttf) untouched | Lands on BID 200 as a second xref (would mean the key is not source-scoped) |
| R4-7 rerun (optional) | 4a again | identical file | 0 new events | Events repeat |

Mixed good and bad rows: R4-1, R4-2, R4-5 are good rows in the same file as bad R4-3 and R4-4.
Check the import job counts: expect success 3, failed 2 if rejects are per row.

touch_notes: R4-1 and R4-2 carry new UC-FI*-R4 labels. R4-3, R4-4 and R4-5 deliberately keep the
guest's CURRENT touch_notes so a rejected row or a no-op changes nothing visible.
