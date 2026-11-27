package com.visakha.txmdmext.store;

import com.visakha.txmdmext.event.StoredEvent;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Reads and stamps ZLV_MDM_EVENTS_STORE. Never deletes from it. */
@Repository
public class EventStoreRepository {

  static final String FIND_UNPROCESSED =
      """
      SELECT id01, evt_dtm, payload
        FROM ZLV_MDM_EVENTS_STORE
       WHERE processed_dtm IS NULL
       ORDER BY evt_dtm, id01
       FETCH FIRST :limit ROWS ONLY
      """;

  // Stamps every duplicate row of the same ID01 at once.
  static final String MARK_PROCESSED =
      """
      UPDATE ZLV_MDM_EVENTS_STORE
         SET processed_dtm = :processedDtm
       WHERE id01 = :id01
         AND processed_dtm IS NULL
      """;

  private final JdbcClient jdbc;

  public EventStoreRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public List<StoredEvent> findUnprocessed(int limit) {
    return jdbc.sql(FIND_UNPROCESSED)
        .param("limit", limit)
        .query((rs, n) -> {
          long evtDtm = rs.getLong("evt_dtm");
          return new StoredEvent(
              rs.getString("id01"), rs.wasNull() ? null : evtDtm, rs.getString("payload"));
        })
        .list();
  }

  public int markProcessed(String id01, LocalDateTime processedDtm) {
    return jdbc.sql(MARK_PROCESSED)
        .param("processedDtm", processedDtm, Types.TIMESTAMP)
        .param("id01", id01, Types.VARCHAR)
        .update();
  }
}
