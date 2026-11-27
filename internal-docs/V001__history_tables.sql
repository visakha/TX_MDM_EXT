-- V001__history_tables.sql
-- History tables for zlv_guest business events (TX_MDM_EXT).
-- Same Oracle schema as ZLV_MDM_EVENTS_STORE. Oracle 12.2+ (identity columns).
--
-- HARD RULE: CREATE only. No DROP, no TRUNCATE, no DELETE in this file, ever.
-- Run by hand (Vamsi / DBA). The app never runs DDL.
-- Re-running fails with ORA-00955 (name already used) - that is safe, nothing is lost.
--
-- Type 2 convention for *_HIST tables:
--   valid_from = event time (dateOfChange), valid_to NULL = current row.
--   A new version closes the old row (sets valid_to, is_current = 0) and inserts a new one.
--   Rows are never deleted. A removed child row gets a final version with is_deleted = 1.
-- Lookups are stored as two columns: <field>_cd (Code) and <field>_nm (Name).
-- Booleans are NUMBER(1): 1 = true, 0 = false, NULL = not supplied.
-- id01 points back to ZLV_MDM_EVENTS_STORE.id01 (the event that produced the row).

------------------------------------------------------------------------
-- 1. Master history: one row per master per event (root fields flattened)
------------------------------------------------------------------------
CREATE TABLE EVT_MASTER_HIST (
    hist_id               NUMBER GENERATED ALWAYS AS IDENTITY,
    business_id           VARCHAR2(64)   NOT NULL,
    active_business_id    VARCHAR2(64),
    id01                  VARCHAR2(255)  NOT NULL,
    event_type            VARCHAR2(64),
    implied_operation     VARCHAR2(32),
    correlation_id        VARCHAR2(128),
    date_of_change        NUMBER(19),
    user_name             VARCHAR2(255),
    event_source_system   VARCHAR2(128),
    merged_business_ids   VARCHAR2(4000),
    state_base            VARCHAR2(32),
    state_validation      VARCHAR2(32),
    state_create_mode     VARCHAR2(32),
    meta_status           VARCHAR2(32),
    meta_created_by       VARCHAR2(255),
    meta_creation_date    TIMESTAMP(3),
    meta_updated_by       VARCHAR2(255),
    meta_last_update_date TIMESTAMP(3),
    first_name            VARCHAR2(255),
    clns_first_name       VARCHAR2(255),
    last_name             VARCHAR2(255),
    clns_last_name        VARCHAR2(255),
    ffr_first_name        VARCHAR2(255),
    ffr_last_name         VARCHAR2(255),
    do_not_contact_ind    NUMBER(1),
    linkedin_url          VARCHAR2(255),
    last_touch_dtm        TIMESTAMP(3),
    prev_last_touch_dtm   TIMESTAMP(3),
    touch_incr            NUMBER,
    touch_notes           VARCHAR2(255),
    touch_cd_cd           VARCHAR2(64),
    touch_cd_nm           VARCHAR2(255),
    is_deleted            NUMBER(1)      DEFAULT 0 NOT NULL,
    payload_json          CLOB,
    valid_from            TIMESTAMP(3)   NOT NULL,
    valid_to              TIMESTAMP(3),
    is_current            NUMBER(1)      DEFAULT 1 NOT NULL,
    load_dtm              TIMESTAMP(3)   DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_evt_master_hist PRIMARY KEY (hist_id),
    CONSTRAINT uq_evt_master_hist UNIQUE (id01, business_id)
);
CREATE INDEX ix_evt_master_hist_bid ON EVT_MASTER_HIST (business_id, is_current);

------------------------------------------------------------------------
-- 2. Master child history (field groups). Keyed by business_id + child _id
------------------------------------------------------------------------
CREATE TABLE EVT_MASTER_PHONE_HIST (
    hist_id        NUMBER GENERATED ALWAYS AS IDENTITY,
    business_id    VARCHAR2(64)  NOT NULL,
    child_id       VARCHAR2(64)  NOT NULL,
    id01           VARCHAR2(255) NOT NULL,
    cntry_cd       NUMBER(10),
    ph_num         NUMBER(20),
    ph_typ_cd      VARCHAR2(64),
    ph_typ_nm      VARCHAR2(255),
    no_mrkt_calls  NUMBER(1),
    is_deleted     NUMBER(1)     DEFAULT 0 NOT NULL,
    valid_from     TIMESTAMP(3)  NOT NULL,
    valid_to       TIMESTAMP(3),
    is_current     NUMBER(1)     DEFAULT 1 NOT NULL,
    load_dtm       TIMESTAMP(3)  DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_evt_master_phone_hist PRIMARY KEY (hist_id),
    CONSTRAINT uq_evt_master_phone_hist UNIQUE (id01, business_id, child_id)
);
CREATE INDEX ix_evt_master_phone_hist_bid ON EVT_MASTER_PHONE_HIST (business_id, child_id, is_current);

CREATE TABLE EVT_MASTER_SRC_ADDR_HIST (
    hist_id              NUMBER GENERATED ALWAYS AS IDENTITY,
    business_id          VARCHAR2(64)  NOT NULL,
    child_id             VARCHAR2(64)  NOT NULL,
    id01                 VARCHAR2(255) NOT NULL,
    address_line_1       VARCHAR2(255),
    address_line_2       VARCHAR2(255),
    address_line_3       VARCHAR2(255),
    sub_bldg_nm          VARCHAR2(255),
    bldg_name            VARCHAR2(255),
    nearest_airport      VARCHAR2(255),
    bldg_mgmnt_company   VARCHAR2(255),
    city                 VARCHAR2(255),
    state_cd_cd          VARCHAR2(64),
    state_cd_nm          VARCHAR2(255),
    address_status_cd_cd VARCHAR2(64),
    address_status_cd_nm VARCHAR2(255),
    address_typ_cd       VARCHAR2(64),
    address_typ_nm       VARCHAR2(255),
    postal_cd            NUMBER(10),
    country_cd_cd        VARCHAR2(64),
    country_cd_nm        VARCHAR2(255),
    is_deleted           NUMBER(1)     DEFAULT 0 NOT NULL,
    valid_from           TIMESTAMP(3)  NOT NULL,
    valid_to             TIMESTAMP(3),
    is_current           NUMBER(1)     DEFAULT 1 NOT NULL,
    load_dtm             TIMESTAMP(3)  DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_evt_master_src_addr_hist PRIMARY KEY (hist_id),
    CONSTRAINT uq_evt_master_src_addr_hist UNIQUE (id01, business_id, child_id)
);
CREATE INDEX ix_evt_master_src_addr_hist_bid ON EVT_MASTER_SRC_ADDR_HIST (business_id, child_id, is_current);

-- OOTB Address group (not x_src_address)
CREATE TABLE EVT_MASTER_ADDR_HIST (
    hist_id                  NUMBER GENERATED ALWAYS AS IDENTITY,
    business_id              VARCHAR2(64)  NOT NULL,
    child_id                 VARCHAR2(64)  NOT NULL,
    id01                     VARCHAR2(255) NOT NULL,
    default_indicator        NUMBER(1),
    address_type_cd          VARCHAR2(64),
    address_type_nm          VARCHAR2(255),
    usage_type_cd            VARCHAR2(64),
    usage_type_nm            VARCHAR2(255),
    address_line1            VARCHAR2(255),
    address_line2            VARCHAR2(255),
    address_line3            VARCHAR2(255),
    city                     VARCHAR2(255),
    county                   VARCHAR2(255),
    state_cd                 VARCHAR2(64),
    state_nm                 VARCHAR2(255),
    country_cd               VARCHAR2(64),
    country_nm               VARCHAR2(255),
    postal_code              VARCHAR2(64),
    postal_code_extension    VARCHAR2(64),
    coordinate_system        VARCHAR2(255),
    location_coordinate_desc VARCHAR2(255),
    address_status_cd        VARCHAR2(64),
    address_status_nm        VARCHAR2(255),
    latitude                 VARCHAR2(64),
    longitude                VARCHAR2(64),
    effective_start_date     TIMESTAMP(3),
    effective_end_date       TIMESTAMP(3),
    enriched_indicator_cd    VARCHAR2(64),
    enriched_indicator_nm    VARCHAR2(255),
    status_code              VARCHAR2(255),
    status_message           VARCHAR2(4000),
    elevation                VARCHAR2(64),
    is_deleted               NUMBER(1)     DEFAULT 0 NOT NULL,
    valid_from               TIMESTAMP(3)  NOT NULL,
    valid_to                 TIMESTAMP(3),
    is_current               NUMBER(1)     DEFAULT 1 NOT NULL,
    load_dtm                 TIMESTAMP(3)  DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_evt_master_addr_hist PRIMARY KEY (hist_id),
    CONSTRAINT uq_evt_master_addr_hist UNIQUE (id01, business_id, child_id)
);
CREATE INDEX ix_evt_master_addr_hist_bid ON EVT_MASTER_ADDR_HIST (business_id, child_id, is_current);

CREATE TABLE EVT_MASTER_ALT_ID_HIST (
    hist_id             NUMBER GENERATED ALWAYS AS IDENTITY,
    business_id         VARCHAR2(64)  NOT NULL,
    child_id            VARCHAR2(64)  NOT NULL,
    id01                VARCHAR2(255) NOT NULL,
    alt_id_val          VARCHAR2(255),
    alt_id_typ_cd       VARCHAR2(64),
    alt_id_typ_nm       VARCHAR2(255),
    active_ind          NUMBER(1),
    start_date          DATE,
    pri_segment_typ_cd  VARCHAR2(64),
    pri_segment_typ_nm  VARCHAR2(255),
    sec_segment_typ_cd  VARCHAR2(64),
    sec_segment_typ_nm  VARCHAR2(255),
    user_notes          VARCHAR2(255),
    is_deleted          NUMBER(1)     DEFAULT 0 NOT NULL,
    valid_from          TIMESTAMP(3)  NOT NULL,
    valid_to            TIMESTAMP(3),
    is_current          NUMBER(1)     DEFAULT 1 NOT NULL,
    load_dtm            TIMESTAMP(3)  DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_evt_master_alt_id_hist PRIMARY KEY (hist_id),
    CONSTRAINT uq_evt_master_alt_id_hist UNIQUE (id01, business_id, child_id)
);
CREATE INDEX ix_evt_master_alt_id_hist_bid ON EVT_MASTER_ALT_ID_HIST (business_id, child_id, is_current);

------------------------------------------------------------------------
-- 3. Field-level change log: one row per dataDiff entry (append only)
------------------------------------------------------------------------
CREATE TABLE EVT_FIELD_CHANGE (
    change_id       NUMBER GENERATED ALWAYS AS IDENTITY,
    id01            VARCHAR2(255)  NOT NULL,
    seq_no          NUMBER(6)      NOT NULL,
    business_id     VARCHAR2(64)   NOT NULL,
    field_path      VARCHAR2(1000) NOT NULL,
    field_group     VARCHAR2(128),
    child_id        VARCHAR2(64),
    field_name      VARCHAR2(128),
    change_type     VARCHAR2(16),
    old_value       VARCHAR2(4000),
    new_value       VARCHAR2(4000),
    date_of_change  NUMBER(19),
    change_dtm      TIMESTAMP(3),
    load_dtm        TIMESTAMP(3)   DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_evt_field_change PRIMARY KEY (change_id),
    CONSTRAINT uq_evt_field_change UNIQUE (id01, seq_no)
);
CREATE INDEX ix_evt_field_change_bid ON EVT_FIELD_CHANGE (business_id, change_dtm);

------------------------------------------------------------------------
-- 4. Source side
------------------------------------------------------------------------
-- 4a. Master-source membership history (from members[]), Type 2
CREATE TABLE EVT_MEMBER_HIST (
    hist_id         NUMBER GENERATED ALWAYS AS IDENTITY,
    business_id     VARCHAR2(64)  NOT NULL,
    source_system   VARCHAR2(128) NOT NULL,
    source_pkey     VARCHAR2(255) NOT NULL,
    id01_opened     VARCHAR2(255) NOT NULL,
    id01_closed     VARCHAR2(255),
    valid_from      TIMESTAMP(3)  NOT NULL,
    valid_to        TIMESTAMP(3),
    is_current      NUMBER(1)     DEFAULT 1 NOT NULL,
    load_dtm        TIMESTAMP(3)  DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_evt_member_hist PRIMARY KEY (hist_id),
    CONSTRAINT uq_evt_member_hist UNIQUE (id01_opened, business_id, source_system, source_pkey)
);
CREATE INDEX ix_evt_member_hist_src ON EVT_MEMBER_HIST (source_system, source_pkey, is_current);
CREATE INDEX ix_evt_member_hist_bid ON EVT_MEMBER_HIST (business_id, is_current);

-- 4b. Source record change log (from affectedXrefs[]), append only
CREATE TABLE EVT_XREF_CHANGE (
    change_id          NUMBER GENERATED ALWAYS AS IDENTITY,
    id01               VARCHAR2(255) NOT NULL,
    business_id        VARCHAR2(64),
    source_system      VARCHAR2(128) NOT NULL,
    source_pkey        VARCHAR2(255) NOT NULL,
    xref_type          VARCHAR2(16),
    xref_change        VARCHAR2(16),
    membership_change  VARCHAR2(16),
    implied_operation  VARCHAR2(32),
    date_of_change     NUMBER(19),
    change_dtm         TIMESTAMP(3),
    load_dtm           TIMESTAMP(3)  DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_evt_xref_change PRIMARY KEY (change_id),
    CONSTRAINT uq_evt_xref_change UNIQUE (id01, source_system, source_pkey)
);
CREATE INDEX ix_evt_xref_change_src ON EVT_XREF_CHANGE (source_system, source_pkey, change_dtm);

-- 4c. Source record value history, Type 2.
-- Events carry only source keys, not source values. This table is filled by
-- reading the B360 source-record API for each affected xref after the event.
CREATE TABLE EVT_SOURCE_HIST (
    hist_id         NUMBER GENERATED ALWAYS AS IDENTITY,
    source_system   VARCHAR2(128) NOT NULL,
    source_pkey     VARCHAR2(255) NOT NULL,
    business_id     VARCHAR2(64),
    id01            VARCHAR2(255) NOT NULL,
    xref_type       VARCHAR2(16),
    is_deleted      NUMBER(1)     DEFAULT 0 NOT NULL,
    source_json     CLOB,
    valid_from      TIMESTAMP(3)  NOT NULL,
    valid_to        TIMESTAMP(3),
    is_current      NUMBER(1)     DEFAULT 1 NOT NULL,
    load_dtm        TIMESTAMP(3)  DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_evt_source_hist PRIMARY KEY (hist_id),
    CONSTRAINT uq_evt_source_hist UNIQUE (id01, source_system, source_pkey)
);
CREATE INDEX ix_evt_source_hist_src ON EVT_SOURCE_HIST (source_system, source_pkey, is_current);
