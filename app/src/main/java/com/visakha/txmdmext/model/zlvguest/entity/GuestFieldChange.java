package com.visakha.txmdmext.model.zlvguest.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** One row of EVT_ZLV_GUEST_FIELD_CHANGE (ddl/V002): one dataDiff entry, append only. */
@Table(GuestFieldChange.TABLE)
public record GuestFieldChange(
    @Id @Column("change_id") Long changeId,
    @Column("id01") String id01,
    @Column("seq_no") Integer seqNo,
    @Column("business_id") String businessId,
    @Column("field_path") String fieldPath,
    @Column("field_group") String fieldGroup,
    @Column("child_id") String childId,
    @Column("field_name") String fieldName,
    @Column("change_type") String changeType,
    @Column("old_value") String oldValue,
    @Column("new_value") String newValue,
    @Column("date_of_change") Long dateOfChange,
    @Column("change_dtm") LocalDateTime changeDtm,
    @ReadOnlyProperty @Column("load_dtm") LocalDateTime loadDtm) {

  public static final String TABLE = "EVT_ZLV_GUEST_FIELD_CHANGE";
}
