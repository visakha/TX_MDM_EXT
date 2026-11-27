package com.visakha.txmdmext.store;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Idempotency: has any history table already got a row from this ID01? With row_md5 CDC an event
 * may write no master row, so every table is checked.
 */
@Repository
public class LoadedEventRepository {

  static final String COUNT_ROWS_OF_ID01 =
      """
      SELECT (SELECT COUNT(*) FROM EVT_MASTER_HIST WHERE id01 = :id01)
           + (SELECT COUNT(*) FROM EVT_MASTER_PHONE_HIST WHERE id01 = :id01)
           + (SELECT COUNT(*) FROM EVT_MASTER_SRC_ADDR_HIST WHERE id01 = :id01)
           + (SELECT COUNT(*) FROM EVT_MASTER_ADDR_HIST WHERE id01 = :id01)
           + (SELECT COUNT(*) FROM EVT_MASTER_ALT_ID_HIST WHERE id01 = :id01)
           + (SELECT COUNT(*) FROM EVT_FIELD_CHANGE WHERE id01 = :id01)
           + (SELECT COUNT(*) FROM EVT_XREF_CHANGE WHERE id01 = :id01)
           + (SELECT COUNT(*) FROM EVT_MEMBER_HIST
               WHERE id01_opened = :id01 OR id01_closed = :id01) AS row_count
        FROM DUAL
      """;

  private final JdbcClient jdbc;

  public LoadedEventRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public boolean alreadyLoaded(String id01) {
    Number count = jdbc.sql(COUNT_ROWS_OF_ID01).param("id01", id01).query(Number.class).single();
    return count != null && count.longValue() > 0;
  }
}
