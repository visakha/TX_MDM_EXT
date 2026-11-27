package com.visakha.txmdmext.model.zlvguest.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * One row of EVT_ZLV_GUEST_MASTER_HIST (ddl/V002): Type 2, one row per master per event. histId is
 * the identity column (null before insert). loadDtm is filled by the database default.
 */
@Table(GuestMasterHist.TABLE)
public record GuestMasterHist(
    @Id @Column("hist_id") Long histId,
    @Column("business_id") String businessId,
    @Column("active_business_id") String activeBusinessId,
    @Column("id01") String id01,
    @Column("event_type") String eventType,
    @Column("implied_operation") String impliedOperation,
    @Column("correlation_id") String correlationId,
    @Column("date_of_change") Long dateOfChange,
    @Column("user_name") String userName,
    @Column("event_source_system") String eventSourceSystem,
    @Column("merged_business_ids") String mergedBusinessIds,
    @Column("state_base") String stateBase,
    @Column("state_validation") String stateValidation,
    @Column("state_create_mode") String stateCreateMode,
    @Column("meta_status") String metaStatus,
    @Column("meta_created_by") String metaCreatedBy,
    @Column("meta_creation_date") LocalDateTime metaCreationDate,
    @Column("meta_updated_by") String metaUpdatedBy,
    @Column("meta_last_update_date") LocalDateTime metaLastUpdateDate,
    @Column("first_name") String firstName,
    @Column("clns_first_name") String clnsFirstName,
    @Column("last_name") String lastName,
    @Column("clns_last_name") String clnsLastName,
    @Column("ffr_first_name") String ffrFirstName,
    @Column("ffr_last_name") String ffrLastName,
    @Column("do_not_contact_ind") Integer doNotContactInd,
    @Column("linkedin_url") String linkedinUrl,
    @Column("last_touch_dtm") LocalDateTime lastTouchDtm,
    @Column("prev_last_touch_dtm") LocalDateTime prevLastTouchDtm,
    @Column("touch_incr") Double touchIncr,
    @Column("touch_notes") String touchNotes,
    @Column("touch_cd_cd") String touchCdCd,
    @Column("touch_cd_nm") String touchCdNm,
    @Column("is_deleted") Integer isDeleted,
    @Column("payload_json") String payloadJson,
    @Column("row_md5") String rowMd5,
    @Column("valid_from") LocalDateTime validFrom,
    @Column("valid_to") LocalDateTime validTo,
    @Column("is_current") Integer isCurrent,
    @ReadOnlyProperty @Column("load_dtm") LocalDateTime loadDtm) {

  public static final String TABLE = "EVT_ZLV_GUEST_MASTER_HIST";

  /** The same row, closed: valid_to set, is_current = 0. Saved back as an UPDATE. */
  public GuestMasterHist closed(LocalDateTime closedAt) {
    return new GuestMasterHist(
        histId,
        businessId,
        activeBusinessId,
        id01,
        eventType,
        impliedOperation,
        correlationId,
        dateOfChange,
        userName,
        eventSourceSystem,
        mergedBusinessIds,
        stateBase,
        stateValidation,
        stateCreateMode,
        metaStatus,
        metaCreatedBy,
        metaCreationDate,
        metaUpdatedBy,
        metaLastUpdateDate,
        firstName,
        clnsFirstName,
        lastName,
        clnsLastName,
        ffrFirstName,
        ffrLastName,
        doNotContactInd,
        linkedinUrl,
        lastTouchDtm,
        prevLastTouchDtm,
        touchIncr,
        touchNotes,
        touchCdCd,
        touchCdNm,
        isDeleted,
        payloadJson,
        rowMd5,
        validFrom,
        closedAt,
        HistFlags.NO,
        loadDtm);
  }
}
