package com.visakha.txmdmext.store;

import com.visakha.txmdmext.history.FieldChangeRow;
import java.sql.Types;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** EVT_FIELD_CHANGE: one row per dataDiff entry, append only. */
@Repository
public class FieldChangeRepository {

  static final String INSERT =
      """
      INSERT INTO EVT_FIELD_CHANGE (
        id01, seq_no, business_id, field_path, field_group, child_id, field_name,
        change_type, old_value, new_value, date_of_change, change_dtm)
      VALUES (
        :id01, :seqNo, :businessId, :fieldPath, :fieldGroup, :childId, :fieldName,
        :changeType, :oldValue, :newValue, :dateOfChange, :changeDtm)
      """;

  private final JdbcClient jdbc;
  private final DbTime time;

  public FieldChangeRepository(JdbcClient jdbc, DbTime time) {
    this.jdbc = jdbc;
    this.time = time;
  }

  public void insert(FieldChangeRow c) {
    jdbc.sql(INSERT)
        .param("id01", c.id01(), Types.VARCHAR)
        .param("seqNo", c.seqNo(), Types.INTEGER)
        .param("businessId", c.businessId(), Types.VARCHAR)
        .param("fieldPath", c.fieldPath(), Types.VARCHAR)
        .param("fieldGroup", c.fieldGroup(), Types.VARCHAR)
        .param("childId", c.childId(), Types.VARCHAR)
        .param("fieldName", c.fieldName(), Types.VARCHAR)
        .param("changeType", c.changeType(), Types.VARCHAR)
        .param("oldValue", c.oldValue(), Types.VARCHAR)
        .param("newValue", c.newValue(), Types.VARCHAR)
        .param("dateOfChange", c.dateOfChange(), Types.BIGINT)
        .param("changeDtm", time.of(c.changeTime()), Types.TIMESTAMP)
        .update();
  }
}
