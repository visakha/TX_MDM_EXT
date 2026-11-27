# Test data log - zlv_guest events (2026-11-27)

Written by Muse through the tx-mdm backend on Vamsi's laptop (Tailscale), API calls only, one write at a time, read back after each.
Every created/updated source record has touch_notes starting with its UC label. Nothing deleted or purged. Publish job, ingress, egress, match-merge and CDI NOT run.
Existing guests (JackT 0180, Henry 0190, 0171, 0160) NOT touched.

| # | UC | Action | Source | Business id(s) | Source pkey(s) | What changed | touch_notes | Time CST | Result |
|---|---|---|---|---|---|---|---|---|---|
| 1 | UC7-ttf | CREATE | zlv_ttf | 100000000000172 | EVT-UC7-TTF-1 | new guest with 1 phone | UC7-ttf CREATE via source zlv_ttf - Muse test 2026-11-27 | 00:19:50 | CREATED (read back OK, 1 phone) |
| 2 | UC7-eci | CREATE | zlv_eci | 100000000000192 | EVT-UC7-ECI-1 | new guest | UC7-eci CREATE via source zlv_eci - Muse test 2026-11-27 | 00:20:19 | CREATED |
| 3 | UC6 | CREATE | c360.default.system | 100000000000183 | EVT-UC6-UI-1 | new guest | UC6 CREATE via c360.default.system (UI source) - Muse test 2026-11-27 | 00:20:22 | CREATED |
| 4 | UC12a | CREATE | zlv_ttf | 100000000000162 | EVT-UC12A-TTF-1 | new guest | UC12a MERGE ttf+ttf - member 1 of 2 - Muse test 2026-11-27 | 00:20:25 | CREATED |
| 5 | UC12a | CREATE | zlv_ttf | 100000000000173 | EVT-UC12A-TTF-2 | new guest | UC12a MERGE ttf+ttf - member 2 of 2 - Muse test 2026-11-27 | 00:20:28 | CREATED |
| 6 | UC12b | CREATE | zlv_eci | 100000000000193 | EVT-UC12B-ECI-1 | new guest | UC12b MERGE eci+eci then UC13-eci unmerge - member 1 - Muse test 2026-11-27 | 00:20:30 | CREATED |
| 7 | UC12b | CREATE | zlv_eci | 100000000000163 | EVT-UC12B-ECI-2 | new guest | UC12b MERGE eci+eci then UC13-eci unmerge - member 2 - Muse test 2026-11-27 | 00:20:33 | CREATED |
| 8 | UC12c | CREATE | zlv_ttf | 100000000000184 | EVT-UC12C-TTF-1 | new guest | UC12c MERGE ttf+eci then UC13-ttf unmerge - ttf member - Muse test 2026-11-27 | 00:20:35 | CREATED |
| 9 | UC12c | CREATE | zlv_eci | 100000000000164 | EVT-UC12C-ECI-1 | new guest | UC12c MERGE ttf+eci then UC13-ttf unmerge - eci member - Muse test 2026-11-27 | 00:20:38 | CREATED |
| 10 | UC8-ttf | UPDATE | zlv_ttf | 100000000000172 | EVT-UC7-TTF-1 | last_name UC7ttf -> UC7ttfUpd | UC8-ttf UPDATE root last_name via source zlv_ttf - Muse test 2026-11-27 | 00:20:51 | OK HTTP 200 |
| 11 | UC8-eci | UPDATE | zlv_eci | 100000000000192 | EVT-UC7-ECI-1 | last_name UC7eci -> UC7eciUpd | UC8-eci UPDATE root last_name via source zlv_eci - Muse test 2026-11-27 | 00:21:02 | OK HTTP 200 |
| 12 | UC5-ttf | UPDATE | zlv_ttf | 100000000000172 | EVT-UC7-TTF-1 | add x_src_address 100 Test Ln, Austin | UC5-ttf ADD x_src_address row via source zlv_ttf - Muse test 2026-11-27 | 00:21:05 | OK HTTP 200 |
| 13 | UC5-eci | UPDATE | zlv_eci | 100000000000192 | EVT-UC7-ECI-1 | add x_src_address 200 Test Ln, Austin | UC5-eci ADD x_src_address row via source zlv_eci - Muse test 2026-11-27 | 00:21:09 | OK HTTP 200 |
| 14 | UC17-ttf | UPDATE | zlv_ttf | 100000000000172 | EVT-UC7-TTF-1 | add phone 5125550711 | UC17-ttf ADD second x_phone row via source zlv_ttf - Muse test 2026-11-27 | 00:21:12 | OK HTTP 200 |
| 15 | UC16-ttf | UPDATE | zlv_ttf | 100000000000172 | EVT-UC7-TTF-1 | remove phone 5125550711 | UC16-ttf REMOVE x_phone row 5125550711 via source zlv_ttf - Muse test 2026-11-27 | 00:21:37 | OK HTTP 200 |
| 16 | UC16a-eci | UPDATE | zlv_eci | 100000000000192 | EVT-UC7-ECI-1 | remove x_src_address 200 Test Ln | UC16a-eci REMOVE x_src_address row via source zlv_eci - Muse test 2026-11-27 | 00:21:41 | OK HTTP 200 |
| 17 | UC19-ttf | UPDATE | zlv_ttf | 100000000000172 | EVT-UC7-TTF-1 | add alt id TIN 9999 | UC19-ttf ADD x_alt_id row via source zlv_ttf - Muse test 2026-11-27 | 00:21:44 | OK HTTP 200 |
| 18 | UC20-eci | UPDATE | zlv_eci | 100000000000192 | EVT-UC7-ECI-1 | ph_typ Mobile -> Fax | UC20-eci LOOKUP change x_phone ph_typ Mobile to Fax via zlv_eci - Muse test 2026-11-27 | 00:21:47 | OK HTTP 200 |
| 19 | UC22-eci | UPDATE | zlv_eci | 100000000000192 | EVT-UC7-ECI-1 | do_not_contact_ind=true, touch_incr=1.5 | UC22-eci BOOLEAN+NUMBER do_not_contact_ind true, touch_incr 1.5 via zlv_eci - Muse test 2026-11-27 | 00:21:51 | OK HTTP 200 |
| 20 | UC21-eci | UPDATE | zlv_eci | 100000000000192 | EVT-UC7-ECI-1 | last_name UC7eciUpd -> empty | UC21-eci CLEAR root last_name to empty via source zlv_eci - Muse test 2026-11-27 | 00:21:54 | OK HTTP 200 |
| 21 | UC12a | MERGE | zlv_ttf+zlv_ttf | 100000000000162 <- 100000000000173 | EVT-UC12A-TTF-1, EVT-UC12A-TTF-2 | merge ttf+ttf, survivor 100000000000162, no field overrides | (unchanged; member touch_notes kept) | 00:22:29 | OK merge |
| 22 | UC12b | MERGE | zlv_eci+zlv_eci | 100000000000193 <- 100000000000163 | EVT-UC12B-ECI-1, EVT-UC12B-ECI-2 | merge, survivor 100000000000193, no field overrides | (unchanged; member touch_notes kept) | 00:22:34 | OK merge members=(200, [{'source_system': 'zlv_eci', 'source_pkey': 'EVT-UC12B-ECI-1'}, |
| 23 | UC12c | MERGE | zlv_ttf+zlv_eci | 100000000000184 <- 100000000000164 | EVT-UC12C-TTF-1, EVT-UC12C-ECI-1 | merge, survivor 100000000000184, no field overrides | (unchanged; member touch_notes kept) | 00:22:38 | OK merge members=(200, [{'source_system': 'zlv_eci', 'source_pkey': 'EVT-UC12C-ECI-1'}, |
| 24 | UC26 | UPDATE | zlv_eci | 100000000000164 | EVT-UC12C-ECI-1 | eci member last_name UC12c2 -> UC26eciOnly (master has ttf+eci) | UC26 CROSS-SOURCE update only the zlv_eci member of merged ttf+eci master 100000000000184 - Muse test 2026-11-27 | 00:22:51 | OK HTTP 200 |
| 25 | UC13-eci | UNMERGE | zlv_eci | 100000000000193 -> new master 100000000000163 | EVT-UC12B-ECI-2 | take zlv_eci member EVT-UC12B-ECI-2 out of 100000000000193, NEVER_MERGE | (unchanged; member touch_notes kept) | 00:22:58 | OK HTTP 200; remaining members of 100000000000193: [{'source_system': 'zlv_eci', 'source_pkey': 'EVT-UC12B-ECI-1'}] |
| 26 | UC13-ttf | UNMERGE | zlv_ttf | 100000000000184 (ttf stayed on 184; eci member back to 100000000000164) | EVT-UC12C-TTF-1 | take zlv_ttf member EVT-UC12C-TTF-1 out of 100000000000184, NEVER_MERGE | (unchanged; member touch_notes kept) | 00:23:04 | OK HTTP 200; remaining members of 100000000000184: [{'source_system': 'zlv_ttf', 'source_pkey': 'EVT-UC12C-TTF-1'}] |

## Publish window
- First write: 2026-11-27 00:19:50 CST = 2026-11-26 22:19:50 Pacific
- Last write:  2026-11-27 00:23:04 CST = 2026-11-26 22:23:04 Pacific
- Suggested Custom window (Pacific, as IDMC shows): 2026-11-26 22:15 to 2026-11-26 22:35. Standard mode should also pick them up since the last run.

## Test guests (final state)
- 100000000000172 zlv_ttf EVT-UC7-TTF-1: UC7-ttf create, UC8-ttf, UC5-ttf, UC17-ttf, UC16-ttf, UC19-ttf (1 phone, 1 x_src_address, 1 x_alt_id)
- 100000000000192 zlv_eci EVT-UC7-ECI-1: UC7-eci create, UC8-eci, UC5-eci, UC16a-eci, UC20-eci, UC22-eci, UC21-eci (last_name empty, phone type Fax, no address)
- 100000000000183 c360.default.system EVT-UC6-UI-1: UC6 create
- 100000000000162 <- 100000000000173: UC12a merged ttf+ttf (still merged)
- 100000000000193 / 100000000000163: UC12b merged eci+eci, then UC13-eci unmerged EVT-UC12B-ECI-2 back to 100000000000163
- 100000000000184 / 100000000000164: UC12c merged ttf+eci (survivor 184), UC26 updated only the eci member, then UC13-ttf unmerge of EVT-UC12C-TTF-1. Read-back: 184 kept the ttf member and the eci member went back to 164 - so the pair is split, but IDMC kept the business id 184 on the ttf side. Watch which ids the UNMERGE event reports.
- Note: unmerge was asynchronous - the first read-back right after the call still showed both members; 15 s later they were split.

## Not done, and why
- UC9 DELETE, UC10 RESTORE, UC11 PURGE: the backend has no delete/restore/purge endpoint (and deletes were out of scope).
- UC14 merge by the match-and-merge job: no match-merge job configured; job runs were out of scope.
- UC15 TRUST_OVERRIDE: merge endpoint can override winners, but not tried (kept merges plain). Could be done next.
- UC18 OOTB Address group: not tried; x_src_address was used for add/remove.
- UC19 edit of an existing alt id, and UC17 add+edit in ONE save: not done this round.
- UC23 VALIDATE (validation FAILED): no validation rule known to trigger it.
- UC25 many events per master: covered incidentally (172 and 192 each got 6-7 events).
- UC27 survivorship switch between sources: not done.
- UC6 is a create on c360.default.system through the API, not a true UI create.
