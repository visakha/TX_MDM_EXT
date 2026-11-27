package com.visakha.txmdmext.model.zlvguest.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** One row of EVT_ZLV_GUEST_MASTER_ALT_ID_HIST (ddl/V002): the x_alt_id group, Type 2. */
@Table(GuestMasterAltIdHist.TABLE)
public record GuestMasterAltIdHist(
    @Id @Column("hist_id") Long histId,
    @Column("business_id") String businessId,
    @Column("child_id") String childId,
    @Column("id01") String id01,
    @Column("alt_id_val") String altIdVal,
    @Column("alt_id_typ_cd") String altIdTypCd,
    @Column("alt_id_typ_nm") String altIdTypNm,
    @Column("active_ind") Integer activeInd,
    @Column("start_date") LocalDate startDate,
    @Column("pri_segment_typ_cd") String priSegmentTypCd,
    @Column("pri_segment_typ_nm") String priSegmentTypNm,
    @Column("sec_segment_typ_cd") String secSegmentTypCd,
    @Column("sec_segment_typ_nm") String secSegmentTypNm,
    @Column("user_notes") String userNotes,
    @Column("is_deleted") Integer isDeleted,
    @Column("row_md5") String rowMd5,
    @Column("valid_from") LocalDateTime validFrom,
    @Column("valid_to") LocalDateTime validTo,
    @Column("is_current") Integer isCurrent,
    @ReadOnlyProperty @Column("load_dtm") LocalDateTime loadDtm) {

  public static final String TABLE = "EVT_ZLV_GUEST_MASTER_ALT_ID_HIST";

  /** The same row, closed: valid_to set, is_current = 0. Saved back as an UPDATE. */
  public GuestMasterAltIdHist closed(LocalDateTime closedAt) {
    return new GuestMasterAltIdHist(
        histId,
        businessId,
        childId,
        id01,
        altIdVal,
        altIdTypCd,
        altIdTypNm,
        activeInd,
        startDate,
        priSegmentTypCd,
        priSegmentTypNm,
        secSegmentTypCd,
        secSegmentTypNm,
        userNotes,
        isDeleted,
        rowMd5,
        validFrom,
        closedAt,
        HistFlags.NO,
        loadDtm);
  }
}
