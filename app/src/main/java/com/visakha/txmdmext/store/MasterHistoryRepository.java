package com.visakha.txmdmext.store;

import com.visakha.txmdmext.history.GuestRoot;
import com.visakha.txmdmext.history.Lookup;
import com.visakha.txmdmext.history.MasterVersion;
import com.visakha.txmdmext.history.MetaInfo;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** EVT_MASTER_HIST: Type 2, one row per master per event. Close and append only. */
@Repository
public class MasterHistoryRepository {

  static final String CURRENT_MD5 =
      """
      SELECT row_md5
        FROM EVT_MASTER_HIST
       WHERE business_id = :businessId
         AND is_current = 1
       ORDER BY valid_from DESC, hist_id DESC
       FETCH FIRST 1 ROWS ONLY
      """;

  static final String CLOSE_CURRENT =
      """
      UPDATE EVT_MASTER_HIST
         SET valid_to = :validTo, is_current = 0
       WHERE business_id = :businessId
         AND is_current = 1
      """;

  static final String INSERT =
      """
      INSERT INTO EVT_MASTER_HIST (
        business_id, active_business_id, id01, event_type, implied_operation, correlation_id,
        date_of_change, user_name, event_source_system, merged_business_ids,
        state_base, state_validation, state_create_mode,
        meta_status, meta_created_by, meta_creation_date, meta_updated_by, meta_last_update_date,
        first_name, clns_first_name, last_name, clns_last_name, ffr_first_name, ffr_last_name,
        do_not_contact_ind, linkedin_url, last_touch_dtm, prev_last_touch_dtm, touch_incr,
        touch_notes, touch_cd_cd, touch_cd_nm,
        is_deleted, payload_json, row_md5, valid_from, valid_to, is_current)
      VALUES (
        :businessId, :activeBusinessId, :id01, :eventType, :impliedOperation, :correlationId,
        :dateOfChange, :userName, :eventSourceSystem, :mergedBusinessIds,
        :stateBase, :stateValidation, :stateCreateMode,
        :metaStatus, :metaCreatedBy, :metaCreationDate, :metaUpdatedBy, :metaLastUpdateDate,
        :firstName, :clnsFirstName, :lastName, :clnsLastName, :ffrFirstName, :ffrLastName,
        :doNotContactInd, :linkedinUrl, :lastTouchDtm, :prevLastTouchDtm, :touchIncr,
        :touchNotes, :touchCdCd, :touchCdNm,
        :isDeleted, :payloadJson, :rowMd5, :validFrom, NULL, 1)
      """;

  private final JdbcClient jdbc;
  private final DbTime time;

  public MasterHistoryRepository(JdbcClient jdbc, DbTime time) {
    this.jdbc = jdbc;
    this.time = time;
  }

  /** row_md5 of the current row; empty when there is none (or it predates row_md5). */
  public Optional<String> currentMd5(String businessId) {
    return jdbc.sql(CURRENT_MD5)
        .param("businessId", businessId)
        .query((rs, n) -> rs.getString("row_md5"))
        .optional()
        .map(String::trim);
  }

  public void closeCurrent(String businessId, LocalDateTime validTo) {
    jdbc.sql(CLOSE_CURRENT)
        .param("validTo", validTo, Types.TIMESTAMP)
        .param("businessId", businessId, Types.VARCHAR)
        .update();
  }

  public void insert(MasterVersion v) {
    MetaInfo m = v.meta();
    GuestRoot r = v.root();
    Lookup touchCd = r.touchCd() == null ? new Lookup(null, null) : r.touchCd();
    jdbc.sql(INSERT)
        .param("businessId", v.businessId(), Types.VARCHAR)
        .param("activeBusinessId", v.activeBusinessId(), Types.VARCHAR)
        .param("id01", v.id01(), Types.VARCHAR)
        .param("eventType", v.eventType(), Types.VARCHAR)
        .param("impliedOperation", v.impliedOperation(), Types.VARCHAR)
        .param("correlationId", v.correlationId(), Types.VARCHAR)
        .param("dateOfChange", v.dateOfChange(), Types.BIGINT)
        .param("userName", v.userName(), Types.VARCHAR)
        .param("eventSourceSystem", v.eventSourceSystem(), Types.VARCHAR)
        .param("mergedBusinessIds", v.mergedBusinessIds(), Types.VARCHAR)
        .param("stateBase", v.stateBase(), Types.VARCHAR)
        .param("stateValidation", v.stateValidation(), Types.VARCHAR)
        .param("stateCreateMode", v.stateCreateMode(), Types.VARCHAR)
        .param("metaStatus", m.status(), Types.VARCHAR)
        .param("metaCreatedBy", m.createdBy(), Types.VARCHAR)
        .param("metaCreationDate", time.of(m.creationDate()), Types.TIMESTAMP)
        .param("metaUpdatedBy", m.updatedBy(), Types.VARCHAR)
        .param("metaLastUpdateDate", time.of(m.lastUpdateDate()), Types.TIMESTAMP)
        .param("firstName", r.firstName(), Types.VARCHAR)
        .param("clnsFirstName", r.clnsFirstName(), Types.VARCHAR)
        .param("lastName", r.lastName(), Types.VARCHAR)
        .param("clnsLastName", r.clnsLastName(), Types.VARCHAR)
        .param("ffrFirstName", r.ffrFirstName(), Types.VARCHAR)
        .param("ffrLastName", r.ffrLastName(), Types.VARCHAR)
        .param("doNotContactInd", DbTime.flag(r.doNotContactInd()), Types.INTEGER)
        .param("linkedinUrl", r.linkedinUrl(), Types.VARCHAR)
        .param("lastTouchDtm", time.of(r.lastTouchDtm()), Types.TIMESTAMP)
        .param("prevLastTouchDtm", time.of(r.prevLastTouchDtm()), Types.TIMESTAMP)
        .param("touchIncr", r.touchIncr(), Types.DOUBLE)
        .param("touchNotes", r.touchNotes(), Types.VARCHAR)
        .param("touchCdCd", touchCd.code(), Types.VARCHAR)
        .param("touchCdNm", touchCd.name(), Types.VARCHAR)
        .param("isDeleted", v.deleted() ? 1 : 0, Types.INTEGER)
        .param("payloadJson", v.payloadJson(), Types.CLOB)
        .param("rowMd5", v.rowMd5(), Types.CHAR)
        .param("validFrom", time.of(v.validFrom()), Types.TIMESTAMP)
        .update();
  }
}
