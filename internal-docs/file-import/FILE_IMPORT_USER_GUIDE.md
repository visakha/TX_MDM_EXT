# Business 360 File Import - User Guide (zlv_guest)

How to load, change and delete zlv_guest records with a CSV through the
Business 360 File Import screen, then check the result with the publish
events extract. Written from tests run in our org on 2026-11-27.

Status of each rule below:
- VERIFIED = tested in our org and the extract proved it.
- DOCS = from Informatica's online help, not yet proven by us.

## 1. The big picture

1. Build a CSV (Section 3).
2. Business 360 > File Import > Import all data > Upload a file.
3. Pick the source system (zlv_ttf or zlv_eci).
4. Map columns to fields (Section 4). Save the mapping to reuse it.
5. Pick the import type (Section 5), preview, Import.
6. Run the publish job mdm_zlv_guest_pub_evts_001.
7. Run the extract (tools/extract/extract.ps1) and check the events.

There is no template to download. You build the CSV yourself. Up to
10,000 records per file (DOCS).

## 2. How a guest is shaped (the relations)

A zlv_guest master record has:

- Root fields: one value each. first_name, last_name, touch_notes, etc.
- Field groups: lists of child entries, zero to many each.
  - x_phone       (ph_typ, cntry_cd, ph_num)
  - Address       (addressType, addressLine1, city, state, postalCode, country)
  - x_src_address (address_line_1, city, state_cd, postal_cd, country_cd,
                   address_typ, address_status_cd)
  - x_alt_id      (alt_id_typ, alt_id_val)

Relations:
- Source record -> master. Each CSV root key (Source Primary Key) plus the
  source system you pick is ONE source record. MDM puts it into a master
  (a business ID). Same source system + same key again = update of the same
  source record, not a new guest. (VERIFIED)
- Child entry -> parent. Every child entry is identified by its own
  Source Primary Key inside its group (for example FI-UC-3-PH-2). MDM keeps
  that key as the child row's _id, and the events show it in
  dataDiff fieldPath, e.g. x_phone[FI-UC-3-PH-2].ph_num. (VERIFIED)
- Groups are independent. A guest can have 3 phones, 0 addresses and
  2 alt ids. One group does not depend on another.

## 3. The CSV format

Rules (VERIFIED unless marked):
- Plain CSV, comma delimited, header row, ASCII only, no quoted fields.
  Avoid commas inside values (use "; " instead).
- Column names are free text; you map them in the wizard. We use
  <group>.<field> so the mapping is obvious.
- Root key column: Source Primary Key.
- Source Last Update Date is required. Accepted formats:
  yyyy-MM-dd HH:mm:ss (use this) or epoch milliseconds.
  REJECTED: yyyy-MM-dd, MM/dd/yyyy, MM/dd/yyyy HH:mm:ss, ISO with Z.
- Each field group needs its own Source Primary Key column
  (x_phone.Source Primary Key, Address.Source Primary Key, ...).
- Address needs country.
- ph_num is an Integer field. Long numbers fail with "invalid format".
  10 digits failed; 6 digits worked. Keep test numbers short.
- Lookup fields must use codes that exist in Reference 360
  (internal-docs/LOOKUPS.md). Codes are case sensitive (VoIP, EMP ID).

### Several children for one guest

Repeat the guest row. Keep the root key and root fields the same, and put
one child entry per group on each row. If a guest has 3 phones and
2 addresses, you can either list every combination (6 rows) or pad short
groups with blanks. MDM keeps one entry per child key, so repeats do not
create copies. (VERIFIED: 30 cross-product rows gave exactly the expected
child counts.)

Example (one guest, 2 phones, 1 address):

    Source Primary Key,Source Last Update Date,first_name,last_name,x_phone.Source Primary Key,x_phone.ph_typ,x_phone.cntry_cd,x_phone.ph_num,Address.Source Primary Key,Address.addressLine1,Address.city,Address.country
    G-1,2026-11-27 09:00:00,Ann,Test,G-1-PH-1,Mobile,1,550001,G-1-AD-1,1 Test Ln,Austin,US
    G-1,2026-11-27 09:00:00,Ann,Test,G-1-PH-2,Fax,1,550002,,,,

## 4. Mapping step

- Map every column, including each group's Operation Type and Source
  Primary Key columns.
- Save the mapping. If the next file keeps the same header, the saved
  mapping still fits.

## 5. Operation types and patching

Operation Type can be set in the wizard (for the whole file) or in the
CSV (per row, per group). The CSV value wins over the wizard. (DOCS)

Values (DOCS):
- Upsert - add new records, or OVERWRITE all fields of existing ones.
  Fields you leave out become null.
- Patch - update record sections. A section = root fields, or one field
  group. Send ALL fields of a section you touch, or the missing ones are
  cleared.
- Delete - delete a field group entry, a whole source record,
  relationships or hierarchies. It cannot delete root field values.

Wizard choices (DOCS):
- Create or overwrite records = Upsert.
- Update record sections = Patch.
- Update only fields containing data = changes only non-empty cells; you
  choose whether empty cells null the field or leave it unchanged.

What we saw (VERIFIED):
- Adds: a new child key in a group = new child entry.
- Edits: same child key with new values = in-place update
  (phone type Cell -> Mobile, zip 78703 -> 78731 both worked).

### Deleting child entries

- Put the delete on its OWN row: root Operation Type = Patch, root
  Source Primary Key, Source Last Update Date, the guest's CURRENT root
  values (first_name, last_name, touch_notes - same on every row of that
  guest), the group's Source Primary Key of the entry to remove, and
  Delete in that group's Operation Type. Leave the other groups blank.
- There is a root-level Operation Type column (header "Operation Type")
  as well as one per group. Root = Patch keeps the guest; root = Delete
  removes the whole source record.
- One deletion per row. Address and x_src_address are separate groups, so
  removing "the same" address from both takes two rows.
- Use the wizard choice "Update only fields containing data" with empty
  fields left unchanged, so the blank root columns do not wipe the guest.
- Do NOT put Delete in the ROOT Operation Type: that deletes the whole
  source record.

Lesson from round 2 (VERIFIED): Delete marks placed on the same rows as
kept children (inside a cross-product file) were ignored - all adds and
edits landed, none of the 7 deletes did. Round 3 uses the separate-row
layout above; its result is pending.

Restore of a deleted source record is also an operation (DOCS, not tested).

## 6. Checking the result

1. Run the publish job (Standard mode picks up changes since last run).
2. Extract: see tools/extract/README.md.
3. In ZLV_MDM_EVENTS_STORE, dedupe on ID01 (repeat publishes duplicate
   rows). Then per guest expect:
   - new guest: MASTER_CREATED / CREATE, xref zlv_ttf DATA CREATED.
   - change: MASTER_UPDATED / UPDATE, dataDiff entries:
     CREATED (added child), UPDATED (edited field, lookups show .Code and
     .Name separately), DELETED (removed child or cleared field,
     newValue null).
   - payload = the master AFTER the change, child lists complete.

## 7. Worked example files (internal-docs/file-import)

- zlv_guest_file_import_5x.csv - round 1, 5 guests FI-UC-1..5, 30 rows,
  creates. VERIFIED: BIDs 100000000000200-204, counts exact.
- zlv_guest_file_import_5x_round2.csv - round 2, adds/edits/deletes in
  place. VERIFIED: adds and edits landed, deletes ignored.
- zlv_guest_file_import_5x_round3.csv - round 3, the 9 delete rows only,
  separate-row layout. Pending.

Use touch_notes to label each record with its use case (for example
"UC-FI3-R2 del Mobile PH-1"). It is our field for test notes.

## 8. Random test generator (TX_MDM_EXT app)

To repeat the exercise with fresh data, the app can write a new set of round files plus the
expected result of each round. Same seed = same files, so a teammate can rebuild exactly the
files you used.

    cd TX_MDM_EXT\app
    mvn -B -DskipTests package
    .\generate-file-import.ps1 -Seed 101 -Guests 5 -OutDir C:\tx-mdm-ext\file-import

It writes into C:\tx-mdm-ext\file-import\RG-101\:

- zlv_guest_RG-101_round1.csv - creates (random 0..3 phones, addresses, alt ids).
- zlv_guest_RG-101_round2.csv - adds and edits (phone type, zip) on the same guests.
- zlv_guest_RG-101_round3.csv - deletes only, one per row, root Operation Type = Patch.
- zlv_guest_RG-101_roundN_expected.csv - per guest the child keys and counts after round N.

Give each teammate their own seed: the guest keys are RG-<seed>-<n>, so runs do not collide
with each other or with the FI-UC-* guests. All files share one header, so save the wizard
mapping once and reuse it.

Steps per round: import the CSV under zlv_ttf (round 3: "Update only fields containing data",
empty fields unchanged), run the publish job, extract, and compare each guest's phones /
addresses / alt ids in the event payload with the round's _expected.csv.

The generator only writes files. It does not import, publish or touch any database.
Round 3 (the delete layout) is still pending verification in our org - see Section 5.
