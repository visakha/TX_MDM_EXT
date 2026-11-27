package com.visakha.txmdmext.model.zlvguest.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** One row of EVT_ZLV_GUEST_XREF_CHANGE (ddl/V002): one affectedXrefs entry, append only. */
@Table(GuestXrefChange.TABLE)
public record GuestXrefChange(
    @Id @Column("change_id") Long changeId,
    @Column("id01") String id01,
    @Column("business_id") String businessId,
    @Column("source_system") String sourceSystem,
    @Column("source_pkey") String sourcePkey,
    @Column("xref_type") String xrefType,
    @Column("xref_change") String xrefChange,
    @Column("membership_change") String membershipChange,
    @Column("implied_operation") String impliedOperation,
    @Column("date_of_change") Long dateOfChange,
    @Column("change_dtm") LocalDateTime changeDtm,
    @ReadOnlyProperty @Column("load_dtm") LocalDateTime loadDtm) {

  public static final String TABLE = "EVT_ZLV_GUEST_XREF_CHANGE";
}
