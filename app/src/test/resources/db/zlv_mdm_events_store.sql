-- TEST ONLY: the event store table for the in-memory H2 integration tests.
-- Same columns as Vamsi's Oracle table (ext-docs/Ora_pub_table_ddl.txt), CREATE only.
CREATE TABLE ZLV_MDM_EVENTS_STORE (
    id01            VARCHAR2(255) NOT NULL,
    evt_dtm         NUMBER(19) NULL,
    evt_dtm_tmz     TIMESTAMP NULL,
    payload         CLOB NOT NULL,
    processed_dtm   TIMESTAMP NULL
);
