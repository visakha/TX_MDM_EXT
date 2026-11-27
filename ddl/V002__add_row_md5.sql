-- V002__add_row_md5.sql
-- Adds row_md5 to the 7 Type 2 history tables (change data capture).
-- Run this ONLY if you ran V001 BEFORE row_md5 was added to it (2026-11-27).
-- A fresh install runs the current V001 only; running V002 after it fails with
-- ORA-01430 (column already exists) - that is safe, nothing is lost.
--
-- HARD RULE: ALTER ... ADD only. No DROP, no TRUNCATE, no DELETE in this file, ever.
-- Run by hand (Vamsi / DBA). The app never runs DDL.
-- Existing rows keep row_md5 NULL; the next event for such a record writes a new
-- version (NULL never equals a hash), after that the CDC check works normally.

ALTER TABLE EVT_MASTER_HIST ADD (row_md5 CHAR(32));
ALTER TABLE EVT_MASTER_PHONE_HIST ADD (row_md5 CHAR(32));
ALTER TABLE EVT_MASTER_SRC_ADDR_HIST ADD (row_md5 CHAR(32));
ALTER TABLE EVT_MASTER_ADDR_HIST ADD (row_md5 CHAR(32));
ALTER TABLE EVT_MASTER_ALT_ID_HIST ADD (row_md5 CHAR(32));
ALTER TABLE EVT_MEMBER_HIST ADD (row_md5 CHAR(32));
ALTER TABLE EVT_SOURCE_HIST ADD (row_md5 CHAR(32));
