package com.visakha.txmdmext.model.zlvguest.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * One row of EVT_ZLV_GUEST_MASTER_PHONE_HIST (ddl/V002 + V003): the x_phone field group, Type 2.
 * The phone number is phone_num (TEXT, V003). The legacy ph_num column is not mapped on purpose:
 * the app never reads or writes it.
 */
@Table(GuestMasterPhoneHist.TABLE)
public record GuestMasterPhoneHist(
    @Id @Column("hist_id") Long histId,
    @Column("business_id") String businessId,
    @Column("child_id") String childId,
    @Column("id01") String id01,
    @Column("cntry_cd") Long cntryCd,
    @Column("phone_num") String phoneNum,
    @Column("ph_typ_cd") String phTypCd,
    @Column("ph_typ_nm") String phTypNm,
    @Column("no_mrkt_calls") Integer noMrktCalls,
    @Column("is_deleted") Integer isDeleted,
    @Column("row_md5") String rowMd5,
    @Column("valid_from") LocalDateTime validFrom,
    @Column("valid_to") LocalDateTime validTo,
    @Column("is_current") Integer isCurrent,
    @ReadOnlyProperty @Column("load_dtm") LocalDateTime loadDtm) {

  public static final String TABLE = "EVT_ZLV_GUEST_MASTER_PHONE_HIST";

  /** The same row, closed: valid_to set, is_current = 0. Saved back as an UPDATE. */
  public GuestMasterPhoneHist closed(LocalDateTime closedAt) {
    return new GuestMasterPhoneHist(
        histId,
        businessId,
        childId,
        id01,
        cntryCd,
        phoneNum,
        phTypCd,
        phTypNm,
        noMrktCalls,
        isDeleted,
        rowMd5,
        validFrom,
        closedAt,
        HistFlags.NO,
        loadDtm);
  }
}
