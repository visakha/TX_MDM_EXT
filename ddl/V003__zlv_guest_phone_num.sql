-- V003: zlv_guest x_phone.phone_num (TEXT) replaces the Integer ph_num.
--
-- Why: the Integer field x_phone.ph_num rejected whole B360 file-import files
-- on long phone numbers (11 digits). Vamsi added a new TEXT field phone_num in
-- the x_phone field group on 2026-11-27. From now on the app reads and writes
-- phone_num only.
--
-- Run ONCE, by hand. The app never runs DDL.
-- Additive only: no DROP, no TRUNCATE, no data change.
-- The old ph_num column stays in place, unused (legacy).
-- row_md5 for this table is now computed over phone_num (see
-- txmdmext.md5.EVT_ZLV_GUEST_MASTER_PHONE_HIST in application.properties).

ALTER TABLE EVT_ZLV_GUEST_MASTER_PHONE_HIST ADD phone_num VARCHAR2(64);
