package com.visakha.txmdmext.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.visakha.txmdmext.event.StoredEvent;
import com.visakha.txmdmext.job.BatchResult;
import com.visakha.txmdmext.job.EventProcessor;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** Idempotency and bad input, each on its own fresh in-memory database. */
class H2RerunIT {

  private static Map<String, Long> counts(H2Database db) {
    return H2HistoryIT.TABLES.stream()
        .collect(Collectors.toMap(t -> t, t -> db.count("SELECT COUNT(*) FROM " + t)));
  }

  @Test
  void secondRunReadsNothingAndAddsNoRows() {
    H2Database db = new H2Database().withHistorySchema().withEventStore().withRealEvents();
    EventProcessor processor = db.processor();
    processor.runOnce();
    Map<String, Long> before = counts(db);
    BatchResult again = processor.runOnce();
    assertEquals(0, again.rowsRead());
    assertEquals(before, counts(db));
  }

  @Test
  void reprocessingTheSameEventsAddsNoRows() {
    H2Database db = new H2Database().withHistorySchema().withEventStore().withRealEvents();
    EventProcessor processor = db.processor();
    processor.runOnce();
    Map<String, Long> before = counts(db);
    // Make every event look new again (an UPDATE, never a delete).
    db.jdbc.sql("UPDATE ZLV_MDM_EVENTS_STORE SET processed_dtm = NULL").update();
    BatchResult again = processor.runOnce();
    assertEquals(36, again.alreadyLoaded());
    assertEquals(0, again.loaded());
    assertEquals(before, counts(db));
    assertEquals(
        0, db.count("SELECT COUNT(*) FROM ZLV_MDM_EVENTS_STORE WHERE processed_dtm IS NULL"));
  }

  @Test
  void badPayloadIsReportedAndDoesNotStopTheBatch() {
    H2Database db = new H2Database().withHistorySchema().withEventStore().withRealEvents();
    db.insertEvent(new StoredEvent("BAD-JSON-1", 1L, "{not json"));
    db.insertEvent(new StoredEvent("BAD-BLANK-1", 2L, " "));
    BatchResult r = db.processor().runOnce();
    assertEquals(36, r.loaded());
    assertEquals(
        List.of("BAD-JSON-1", "BAD-BLANK-1"),
        r.failedId01().stream().sorted((a, b) -> b.compareTo(a)).toList());
    // Bad rows stay unprocessed so they show up again (and in the log) on the next pass.
    assertEquals(
        2, db.count("SELECT COUNT(*) FROM ZLV_MDM_EVENTS_STORE WHERE processed_dtm IS NULL"));
    assertTrue(db.count("SELECT COUNT(*) FROM EVT_MASTER_HIST") > 0);
  }

  @Test
  void v002AddsRowMd5ToAnOldV001Schema() {
    H2Database db = new H2Database();
    // Test-only tweak: rebuild the pre-row_md5 V001 by leaving its row_md5 lines out of the text.
    String oldV001 =
        H2Database.read(H2Database.V001)
            .lines()
            .filter(l -> !l.trim().startsWith("row_md5"))
            .collect(Collectors.joining("\n"));
    db.run(oldV001);
    db.run(H2Database.read(H2Database.V002));
    assertEquals(
        7,
        db.count(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(COLUMN_NAME) ="
                + " 'ROW_MD5'"));
  }
}
