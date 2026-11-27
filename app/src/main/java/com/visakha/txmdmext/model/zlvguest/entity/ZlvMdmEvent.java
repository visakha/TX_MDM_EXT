package com.visakha.txmdmext.model.zlvguest.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * One row of ZLV_MDM_EVENTS_STORE, the Oracle target of the zlv_guest Publish Business Events job
 * (ext-docs/Ora_pub_table_ddl.txt). The table has no primary key and IDMC can write the same ID01
 * more than once, so id01 is the @Id for reading only: the app never inserts or deletes here, it
 * only stamps processed_dtm (see ZlvMdmEventRepository.markProcessed).
 */
@Table(ZlvMdmEvent.TABLE)
public record ZlvMdmEvent(
    @Id @Column("id01") String id01,
    @Column("evt_dtm") Long evtDtm,
    @Column("evt_dtm_tmz") LocalDateTime evtDtmTmz,
    @Column("payload") String payload,
    @Column("processed_dtm") LocalDateTime processedDtm) {

  public static final String TABLE = "ZLV_MDM_EVENTS_STORE";
}
