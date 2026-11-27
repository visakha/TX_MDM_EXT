package com.visakha.txmdmext.model.zlvguest.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * One row of EVT_ZLV_GUEST_MEMBER_HIST (ddl/V002): master-source membership, Type 2. id01Opened is
 * the event that opened the membership, id01Closed the event that closed it.
 */
@Table(GuestMemberHist.TABLE)
public record GuestMemberHist(
    @Id @Column("hist_id") Long histId,
    @Column("business_id") String businessId,
    @Column("source_system") String sourceSystem,
    @Column("source_pkey") String sourcePkey,
    @Column("id01_opened") String id01Opened,
    @Column("id01_closed") String id01Closed,
    @Column("row_md5") String rowMd5,
    @Column("valid_from") LocalDateTime validFrom,
    @Column("valid_to") LocalDateTime validTo,
    @Column("is_current") Integer isCurrent,
    @ReadOnlyProperty @Column("load_dtm") LocalDateTime loadDtm) {

  public static final String TABLE = "EVT_ZLV_GUEST_MEMBER_HIST";

  /** The same row, closed by event closedBy: valid_to set, is_current = 0, id01_closed set. */
  public GuestMemberHist closed(String closedBy, LocalDateTime closedAt) {
    return new GuestMemberHist(
        histId,
        businessId,
        sourceSystem,
        sourcePkey,
        id01Opened,
        closedBy,
        rowMd5,
        validFrom,
        closedAt,
        HistFlags.NO,
        loadDtm);
  }
}
