# Round 4 - expected results (all EXPECTED-UNVERIFIED until the extract comes back)

Two files, because the source system is chosen in the wizard for the whole file.

| File | Wizard source | Wizard mode |
|---|---|---|
| zlv_guest_file_import_5x_round4a.csv (5 rows) | zlv_ttf | Update record sections (Patch). Do NOT pick "Update only fields containing data" - case R4-1 needs the blank to count. |
| zlv_guest_file_import_5x_round4b_zlv_eci.csv (8 rows, 7 guests) | zlv_eci | Create or overwrite records (Upsert) |

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
| R4-8 full ECI guest | FI-ECI-1 | new guest, all four groups, phone_num 5125550101 | MASTER_CREATED xref zlv_eci, phone_num present in dataDiff (proves the wizard mapping) | phone_num missing again (mapping still not done) |
| R4-9 leading zero | FI-ECI-2 | LandLine phone, cntry 44, phone_num 02079460000 | phone_num kept as text 02079460000 | leading zero lost (would mean it is still numeric somewhere) |
| R4-10 look-alike | FI-ECI-3 | same name Fitwo Addronly and address 402 Test Ln as TTF guest BID 201 | New separate master; no merge from the import alone (match is its own job) | Merged into BID 201 at import time |
| R4-11 wrong case lookup | FI-ECI-4 | ph_typ 'mobile' lowercase | Rejected or saved with Code only and no Name, like 'Mobil' in R4-3 | Resolved to Mobile (lookup is case-insensitive) |
| R4-12 root only | FI-ECI-5 | no group columns at all | MASTER_CREATED with first_name, last_name, touch_notes only | Rejected for missing groups |
| R4-13 two phones two rows | FI-ECI-6 | same root on 2 rows, Cell PH-1 + Fax PH-2 | One MASTER_CREATED with both phones | Two masters, or only one phone |

Mixed good and bad rows: R4-1, R4-2, R4-5 are good rows in the same file as bad R4-3 and R4-4.
Check the import job counts: expect success 3, failed 2 if rejects are per row.

touch_notes: R4-1 and R4-2 carry new UC-FI*-R4 labels. R4-3, R4-4 and R4-5 deliberately keep the
guest's CURRENT touch_notes so a rejected row or a no-op changes nothing visible.

Round 4b (rebuilt 2026-11-27 at Vamsi's request, ECI focus): R4-6 plus R4-8..R4-13, all Upsert, source zlv_eci. Map x_phone.phone_num in the wizard. Expect 7 MASTER_CREATED events.
